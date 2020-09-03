package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.util.Date;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CASE_NOT_FOUND;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CONVERT_SPG_UNIT_UPDATE_TO_CREATE;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CLOSE_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB;

@Qualifier("Update")
@Service
public class SpgUpdateUnitProcessor implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.UPDATE.toString())
      .surveyName("CENSUS")
      .addressType("SPG")
      .addressLevel("U")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  // TODO This should faile or we should have a test
  // @Autowired
  // private SpgCreateRouter createRouter;

  @Autowired
  private JobService jobService;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.UPDATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("SPG")
          && rmRequest.getAddressLevel().equals("U")
          && (rmRequest.isUndeliveredAsAddress() || (cache != null && cache.existsInFwmt));
    } catch (NullPointerException e) {
      return false;
    }
  }

  // TODO Should this be re-posted in q instead?
  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Date messageReceivedTime) throws GatewayException {
    if (rmRequest.isUndeliveredAsAddress() && cache == null) {
      rerouteAsCreate(rmRequest);
      return;
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendClose(rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Cancel", FAILED_TO_CLOSE_TM_JOB);

    if (response.getStatusCode().value() == 404) {
      eventManager.triggerErrorEvent(this.getClass(), "Case not found within TM", String.valueOf(rmRequest.getCaseId()), CASE_NOT_FOUND);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Case not found");
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_ACK,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().name());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ReopenCaseRequest tmRequest = SpgUpdateConverter.convertUnit(rmRequest, cache);
    response = cometRestClient.sendReopen(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Update", FAILED_TO_UPDATE_TM_JOB);

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache != null) {
      cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_ACK,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().name(),
        "UAA", tmRequest.getUaa().toString(),
        "Blank Q", tmRequest.getBlank().toString());
  }

  private void rerouteAsCreate(FwmtActionInstruction rmRequest) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), CONVERT_SPG_UNIT_UPDATE_TO_CREATE,
        "Case Ref", rmRequest.getCaseRef());

    rmRequest.setActionInstruction(ActionInstructionType.CREATE);
    jobService.processCreate(rmRequest, new Date());
  }
}
