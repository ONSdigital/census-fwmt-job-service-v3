package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalTypeDTO;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.nc.utils.NamedHouseholderRetrieval;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.nc.NcCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Create")
@Service
public class NcHhCreateEnglandAndWales implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .addressLevel("U")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RmRestClient rmRestClient;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private NamedHouseholderRetrieval namedHouseholderRetrieval;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && !rmRequest.getOa().startsWith("N")
          && rmRequest.isNc();
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime)
      throws GatewayException {
    CaseDetailsDTO houseHolderDetails;
    GatewayCache previousDetails = cacheService.getById(rmRequest.getOldCaseId());

    String accessInfo = null;
    String careCodes = null;
    String householder = "";
    String caseId = rmRequest.getCaseId();

    try {
      houseHolderDetails = rmRestClient.getCase(rmRequest.getOldCaseId());
    } catch (RuntimeException e) {
      houseHolderDetails = null;
    }

    if (houseHolderDetails != null && houseHolderDetails.getRefusalReceived() != null
        && houseHolderDetails.getRefusalReceived().equals(RefusalTypeDTO.HARD_REFUSAL)) {
      householder = namedHouseholderRetrieval.getAndSortRmRefusalCases(caseId, houseHolderDetails);
    }

    CaseRequest tmRequest = NcCreateConverter.convertNcEnglandAndWales(rmRequest, cache, householder, previousDetails);

    eventManager.triggerEvent(caseId, COMET_CREATE_PRE_SENDING,
        "Case Ref", tmRequest.getReference(),
        "Original case id", rmRequest.getCaseId(),
        "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, caseId);
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(),
        "Create", FAILED_TO_CREATE_TM_JOB,
        "tmRequest", tmRequest.toString(),
        "rmRequest", rmRequest.toString(),
        "cache", (cache != null) ? cache.toString() : "");

    GatewayCache newCache = cacheService.getById(caseId);

    if (cache != null) {
      careCodes =  cache.getCareCodes();
      accessInfo = cache.getAccessInfo();
    }

    if (newCache == null) {
      cacheService.save(GatewayCache
          .builder()
          .caseId(caseId)
          .originalCaseId(rmRequest.getOldCaseId())
          .existsInFwmt(true)
          .type(10)
          .careCodes(careCodes)
          .accessInfo(accessInfo)
          .lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager
        .triggerEvent(caseId, COMET_CREATE_ACK,
            "Original case id", rmRequest.getCaseId(),
            "Case Ref", rmRequest.getCaseRef(),
            "Response Code", response.getStatusCode().name(),
            "Survey Type", tmRequest.getSurveyType().toString());
  }
}
