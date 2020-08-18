package uk.gov.ons.census.fwmt.jobservice.transition;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
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

  public void processCancelTransition(GatewayCache cache, FwmtCancelActionInstruction rmRequest,
      List<InboundProcessor<FwmtCancelActionInstruction>> processors) throws GatewayException {
    boolean noAction = false;
    boolean processed = false;
    boolean cacheRmRequest;
    String[] actionToTake;
    String message;
    ObjectMapper rmRequestMapper = new ObjectMapper();

    String[] returnedRules = setTransitionRules(cache ,rmRequest.getActionInstruction().toString(), rmRequest.getCaseId());

    actionToTake = returnedRules[0].split(",");

    switch (actionToTake[0]) {
    case "NO_ACTION":
      eventManager
          .triggerEvent(rmRequest.getCaseId(), NO_ACTION_REQUIRED);
      noAction = true;
      break;
    case "REJECT":
      eventManager.triggerErrorEvent(this.getClass(), "Request from RM rejected",
          String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
    case "PROCESS":
      processors.get(0).process(rmRequest, cache);
      if (actionToTake[2].equals("false") && messageCacheService.doesCaseExist(rmRequest.getCaseId())) {
        messageCacheService.deleteByCaseId(rmRequest.getCaseId());
      }
    case "MERGE":
      break;
    default:
      break;
    }

    cacheRmRequest = Boolean.parseBoolean(actionToTake[2]);

    if (noAction && cacheRmRequest) {
      try {
        message = rmRequestMapper.writeValueAsString(rmRequest);
      } catch (JsonProcessingException processFailure){
        return;
      }
      messageCacheService.save(
          MessageCache.builder().caseId(rmRequest.getCaseId()).messageType(actionToTake[1]).message(message).build());
    }
  }

  public void processCreateOrUpdateTransition(GatewayCache cache, FwmtActionInstruction rmRequest,
      List<InboundProcessor<FwmtActionInstruction>> processors) throws GatewayException {
    boolean noAction = false;
    boolean cacheRmRequest;
    String[] actionToTake;
    String message;
    ObjectMapper rmRequestMapper = new ObjectMapper();

    String[] returnedRules = setTransitionRules(cache ,rmRequest.getActionInstruction().toString(), rmRequest.getCaseId());

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
      if (actionToTake[2].equals("false") && messageCacheService.doesCaseExist(rmRequest.getCaseId())) {
        messageCacheService.deleteByCaseId(rmRequest.getCaseId());
      }
      break;
    case "MERGE":
      break;
    default:
      break;
    }

    cacheRmRequest = Boolean.parseBoolean(actionToTake[2]);

    if (noAction && cacheRmRequest) {
      try {
        message = rmRequestMapper.writeValueAsString(rmRequest);
      } catch (JsonProcessingException processFailure){
        return;
      }
      messageCacheService.save(
          MessageCache.builder().caseId(rmRequest.getCaseId()).messageType(actionToTake[1]).message(message).build());
    }
  }

  public String[] setTransitionRules(GatewayCache cache, String actionRequest, String caseId) throws GatewayException {
    String cacheType;
    String recordAge;

    if (cache == null) {
      cacheType = "EMPTY";
      recordAge = "NEWER";
    } else {
      cacheType = cache.lastActionInstruction;
      recordAge = "NEWER";
    }

    String[] returnedRules = transitionRulesLookup.getLookup(cacheType, actionRequest, recordAge);

    if (returnedRules == null) {
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a rule for the create request from RM",
          String.valueOf(caseId), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
          "Could not find a rule for the create request from RM", cache);
    }
    return returnedRules;
  }

  public boolean checkRecordAge() {
    // TODO - Need to add datetime comparison once we are able to obtain the datetime the message was queued
    boolean isNewer = false;
    return isNewer;
  }

  public void mergeRecords() {
  // TODO - Need to add merge code once there is a better understanding of what needs to happen
  }
}