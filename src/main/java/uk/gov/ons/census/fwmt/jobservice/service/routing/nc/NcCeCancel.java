package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CANCEL_TM_JOB;

public class NcCeCancel implements InboundProcessor<FwmtCancelActionInstruction> {

  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";

  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CANCEL.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("E")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Override public ProcessorKey getKey() {
    return key;
  }

    // Note, the originatingCaseId is a new field to be created in cache for NC
    //       it is the original HH Case Id and is to map to a new NC Case Id
    //       this cancel MUST send the NC Case Id to TM!

  @Override
  public boolean isValid(FwmtCancelActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.CANCEL
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("E")
          && rmRequest.isNc()
          && cache.getOriginalCaseId().equals(rmRequest.getCaseId());
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtCancelActionInstruction rmRequest, GatewayCache cache, Instant messageReceivedTime)
      throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_PRE_SENDING,
        "Case Ref", "N/A",
        "Type", "NC Cancel",
        "TM Action", "CLOSE",
        "Source", "RM");

    ResponseEntity<Void> response = cometRestClient.sendClose(rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(),
        "Cancel", FAILED_TO_CANCEL_TM_JOB,
        "rmRequest", rmRequest.toString(),
        "cache", (cache!=null)?cache.toString():"");

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    if (newCache != null) {
      cacheService.save(newCache.toBuilder().lastActionInstruction(rmRequest.getActionInstruction().toString())
          .lastActionTime(messageReceivedTime)
          .build());
    }

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_ACK,
            "Case Ref", "N/A",
            "Type", "NC Cancel",
            "Response Code", response.getStatusCode().name());
  }
}
