package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRulesLookup;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;

import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.NO_ACTION_REQUIRED;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

@Slf4j
@Component
public class Transitioner {
  @Autowired
  private TransitionRulesLookup transitionRulesLookup;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private MessageCacheService messageCacheService;

  public void processTransition(GatewayCache cache, FwmtActionInstruction rmRequest,
      List<InboundProcessor<FwmtActionInstruction>> processors) throws GatewayException {
    boolean noAction = false;
    boolean processed = false;
    String[] actionToTake;
    String cacheType;
    String recordAge;

    if (cache == null) {
      cacheType = "Empty";
      recordAge = "NEWER";
    } else {
      cacheType = cache.lastActionInstruction;
      recordAge = "NEWER";
    }

    String[] returnedRules = transitionRulesLookup.getLookup(cacheType, rmRequest.getActionInstruction().toString(),
        recordAge);

    if (returnedRules == null) {
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a rule for the create request from RM",
          String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
          "Could not find a rule for the create request from RM", cache);
    }

    actionToTake = returnedRules[0].split(",");

    switch (actionToTake[0]) {
    case "NO_ACTION":
      eventManager
          .triggerEvent(rmRequest.getCaseId(), NO_ACTION_REQUIRED,
              "Case Ref", rmRequest.getCaseRef());
      noAction = true;
      break;
    case "REJECT":
      eventManager.triggerErrorEvent(this.getClass(), "Request from RM rejected",
          String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
    case "PROCESS":
      processors.get(0).process(rmRequest, cache);
      processed = true;
    case "MERGE":
      break;
    default:
      break;
    }

    if (noAction && actionToTake[2] == "true") {
      messageCacheService.save(
          MessageCache.builder().caseId(rmRequest.getCaseId()).messageType(actionToTake[1]).build());
    }
    if (processed && actionToTake[2] == "false" ) {
      messageCacheService.deleteByCaseId(rmRequest.getCaseId());
    }
  }
}
