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
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.RM_CANCEL_REQUEST_STORED;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.RM_UPDATE_REQUEST_STORED;


@Slf4j
@Component
public class Transitioner {
  @Autowired
  private TransitionRulesLookup transitionRulesLookup;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private MessageCacheService messageCacheService;

  private String storedCacheType;

  public void processCancelTransition(GatewayCache cache, FwmtCancelActionInstruction rmRequest,
      List<InboundProcessor<FwmtCancelActionInstruction>> processors) throws GatewayException {
    boolean noAction = false;
    boolean cacheRmRequest;
    String message;
    ObjectMapper rmRequestMapper = new ObjectMapper();

    String[] returnedRules = setTransitionRules(cache ,rmRequest.getActionInstruction().toString(), rmRequest.getCaseId());

    switch (returnedRules[0]) {
    case "NO_ACTION":
      eventManager
          .triggerEvent(rmRequest.getCaseId(), NO_ACTION_REQUIRED);
      noAction = true;
      break;
    case "REJECT":
      eventManager.triggerErrorEvent(this.getClass(), "Request from RM rejected",
          String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      break;
    case "PROCESS":
      processors.get(0).process(rmRequest, cache);
      break;
    case "MERGE":
      break;
    default:
      break;
    }

    cacheRmRequest = Boolean.parseBoolean(returnedRules[2]);

    MessageCache messageCache = messageCacheService.getById(rmRequest.getCaseId());

    if (!cacheRmRequest && messageCache != null) {
      messageCacheService.deleteByCaseId(rmRequest.getCaseId());
    }

    if (noAction && cacheRmRequest) {
      try {
        message = rmRequestMapper.writeValueAsString(rmRequest);
      } catch (JsonProcessingException processFailure){
        return;
      }
      if (messageCache == null) {
        messageCacheService.save(
            MessageCache.builder().caseId(rmRequest.getCaseId()).messageType(returnedRules[1]).message(message)
                .build());
      } else {
        messageCacheService.save(
            messageCache.toBuilder().messageType(returnedRules[1]).message(message).build());
      }

      if (storedCacheType.equals("EMPTY")) {
        eventManager
            .triggerEvent(String.valueOf(rmRequest.getCaseId()), RM_CANCEL_REQUEST_STORED,
                "TM Action", "Update received before create case. Update has been stored.");
      }
    }
  }

  public void processCreateOrUpdateTransition(GatewayCache cache, FwmtActionInstruction rmRequest,
        List<InboundProcessor<FwmtActionInstruction>> processors) throws GatewayException {
      boolean noAction = false;
      boolean cacheRmRequest;
      String message;
      String actionInstruction;
      ObjectMapper rmRequestMapper = new ObjectMapper();

      actionInstruction = rmRequest.getActionInstruction().toString();

      String[] returnedRules = setTransitionRules(cache, actionInstruction, rmRequest.getCaseId());

      cacheRmRequest = Boolean.parseBoolean(returnedRules[2]);

      switch (returnedRules[0]) {
      case "NO_ACTION":
        eventManager
            .triggerEvent(rmRequest.getCaseId(), NO_ACTION_REQUIRED,
                "Case Ref", rmRequest.getCaseRef());
        noAction = true;
        break;
      case "REJECT":
        eventManager.triggerErrorEvent(this.getClass(), "Request from RM rejected",
            String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
        break;
      case "PROCESS":
        processors.get(0).process(rmRequest, cache);
        break;
      case "MERGE":
        //TODO - will make a call to mergeRecords() once code is complete
        break;
      default:
        break;
      }

    MessageCache messageCache = messageCacheService.getById(rmRequest.getCaseId());

    if (!cacheRmRequest && messageCache != null) {
        messageCacheService.deleteByCaseId(rmRequest.getCaseId());
      }

      if (noAction && cacheRmRequest) {
        try {
          message = rmRequestMapper.writeValueAsString(rmRequest);
        } catch (JsonProcessingException processFailure) {
          return;
        }

        if (messageCache == null) {
          messageCacheService.save(
              MessageCache.builder().caseId(rmRequest.getCaseId()).messageType(returnedRules[1]).message(message)
                  .build());
        } else {
          messageCacheService.save(
              messageCache.toBuilder().messageType(returnedRules[1]).message(message).build());
        }
      }
      if (actionInstruction.equals("UPDATE") && storedCacheType.equals("EMPTY")) {
        eventManager
            .triggerEvent(String.valueOf(rmRequest.getCaseId()), RM_UPDATE_REQUEST_STORED,
                "Case Ref", rmRequest.getCaseRef(),
                "TM Action", "Update received before create case. Update has been stored.");
      }
  }

  public String[] setTransitionRules(GatewayCache cache, String actionRequest, String caseId) throws GatewayException {
    String cacheType;
    String recordAge;

    if (cache == null) {
      cacheType = "EMPTY";
      recordAge = "NEWER";
    } else {
      //TODO - call to checkRecordAge() will be here once the code is there
      cacheType = cache.lastActionInstruction;
      recordAge = "NEWER";
    }

    storedCacheType = cacheType;

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