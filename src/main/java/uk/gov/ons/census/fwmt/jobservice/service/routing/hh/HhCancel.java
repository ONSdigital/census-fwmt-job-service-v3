package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("Cancel")
@Service
public class HhCancel implements InboundProcessor<FwmtActionInstruction> {

  private static final String PROCESSING = "PROCESSING";

  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";

  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CANCEL.toString())
      .surveyName("CENSUS")
      .addressType("HH")
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

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CANCEL
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && rmRequest.getAddressLevel().equals("U")
          && (cache != null && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime) throws
      GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), PROCESSING,
        "type", "HH Cancel Case",
        "action", "Cancel");

    CasePauseRequest tmRequest = HhCancelConvertor.buildCancel(rmRequest);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendPause(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCodePoo(response, rmRequest.getCaseId(), "Pause", FAILED_TO_CREATE_TM_JOB,
        "tmRequest", tmRequest.toString(),
        "rmRequest", rmRequest.toString(),
        "cache", (cache!=null)?cache.toString():"");

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache != null) {
      cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_ACK,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().name());
  }
}
