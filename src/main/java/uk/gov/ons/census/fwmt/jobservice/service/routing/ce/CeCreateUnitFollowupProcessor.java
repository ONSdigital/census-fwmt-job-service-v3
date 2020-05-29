package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.CeFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Create")
@Service
public class CeCreateUnitFollowupProcessor implements InboundProcessor<FwmtActionInstruction> {
  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private CeFollowUpSchedulingService followUpService;


  private static ProcessorKey key = ProcessorKey.builder()
  .actionInstruction(ActionInstructionType.CREATE.toString())
  .surveyName("CENSUS")
  .addressType("CE")
  .addressLevel("U")
  .build();

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS") 
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("U")
          && !rmRequest.isHandDeliver()
          && (cache == null
          || !(cache.getCaseId().isEmpty() && cache.existsInFwmt));
    } catch (NullPointerException e) {
      return false;
    }
  }
//TODO what do we do with followUpService
//TODO add test for secure
  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
    CaseCreateRequest tmRequest;

    if (rmRequest.isSecureEstablishment()){
      tmRequest = CeCreateConverter.convertCeUnitFollowupSecure(rmRequest, cache);
    }else{
      tmRequest = CeCreateConverter.convertCeUnitFollowup(rmRequest, cache);
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_PRE_SENDING, "Case Ref",
        tmRequest.getReference(), "Survey Type", tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).delivered(true).existsInFwmt(true)
          .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).uprn(rmRequest.getUprn())
          .estabUprn(rmRequest.getEstabUprn()).build());
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_ACK, "Case Ref",
        rmRequest.getCaseRef(), "Response Code", response.getStatusCode().name(), "Survey Type",
        tmRequest.getSurveyType().toString());
  }
}
