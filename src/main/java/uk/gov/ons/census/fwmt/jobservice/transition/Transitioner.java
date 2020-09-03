package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.rabbit.RmFieldRepublishProducer;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ConvertMessage;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.NO_ACTION_REQUIRED;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;


@Slf4j
@Component
public class Transitioner {
  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private MessageCacheService messageCacheService;

  @Autowired
  private RmFieldRepublishProducer rmFieldRepublishProducer;

  @Autowired
  private CacheHeldMessages cacheHeldMessages;

  @Autowired
  private RetrieveTransitionRules retrieveTransitionRules;

  public void processTransition(GatewayCache cache, Object rmRequestReceived,
        InboundProcessor<?> processor, Date messageQueueTime) throws GatewayException {
    boolean isCancel = false;
    SimpleDateFormat reformatDate = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSS");
    String actionInstruction;
    String caseId;
    String caseref;
    FwmtActionInstruction rmRequestCreateUpdate = null;
    FwmtCancelActionInstruction rmRequestCancel = null;
    InboundProcessor<FwmtActionInstruction> processorCreateUpdate = null;
    InboundProcessor<FwmtCancelActionInstruction> processorCancel = null;

    if (rmRequestReceived instanceof FwmtActionInstruction) {
      rmRequestCreateUpdate = (FwmtActionInstruction) rmRequestReceived;
      processorCreateUpdate = (InboundProcessor<FwmtActionInstruction>) processor;
      actionInstruction = rmRequestCreateUpdate.getActionInstruction().toString();
      caseId = rmRequestCreateUpdate.getCaseId();
      caseref = rmRequestCreateUpdate.getCaseRef();
    } else {
      rmRequestCancel = (FwmtCancelActionInstruction) rmRequestReceived;
      processorCancel = (InboundProcessor<FwmtCancelActionInstruction>) processor;
      actionInstruction = rmRequestCancel.getActionInstruction().toString();
      caseId = rmRequestCancel.getCaseId();
      caseref = "";
      isCancel = true;
    }

    try {
      messageQueueTime = reformatDate.parse(messageQueueTime.toString());
    } catch (ParseException e) {
      // TODO: Add error meesage here
    }

    TransitionRule returnedRules = retrieveTransitionRules
        .collectTransitionRules(cache, actionInstruction, caseId, messageQueueTime);

    MessageCache messageCache = messageCacheService.getById(caseId);

    switch (returnedRules.getAction()) {
      case NO_ACTION:
        eventManager
            .triggerEvent(caseId, NO_ACTION_REQUIRED,
                "Case Ref", caseref);
        break;
      case REJECT:
        eventManager.triggerErrorEvent(this.getClass(), "Request from RM rejected",
            String.valueOf(caseId), ROUTING_FAILED);
        break;
      case PROCESS:
        if (isCancel) {
          processorCancel.process(rmRequestCancel, cache, messageQueueTime);
        } else {
          processorCreateUpdate.process(rmRequestCreateUpdate, cache, messageQueueTime);
        }
        break;
      case MERGE:
        mergeRecords(messageCache, cache, messageQueueTime);
        break;
      default:
        //TODO - add default - probably error
        break;
    }

  switch (returnedRules.getRequestAction()) {
    case SAVE:
      if (isCancel) {
        cacheHeldMessages.cacheMessage(messageCache, cache, rmRequestCancel, messageQueueTime);
      } else {
        cacheHeldMessages.cacheMessage(messageCache, cache, rmRequestCreateUpdate, messageQueueTime);
      }
      break;
    case CLEAR:
      if (messageCache != null) {
        messageCacheService.delete(messageCache);
      }
      break;
    default:
      break;
    }
  }

  public void mergeRecords(MessageCache messageCache, GatewayCache gatewayCache, Date messageQueueTime) {
    ConvertMessage convertMessage = new ConvertMessage();
    if (messageCache.messageType.equals("UPDATE(HELD)")) {
      try {
        cacheService.save(gatewayCache.toBuilder().existsInFwmt(true).lastActionInstruction("UPDATE")
        .lastActionTime(messageQueueTime).build());
        FwmtActionInstruction fwmtActionInstruction = convertMessage
            .convertMessageToDTO(FwmtActionInstruction.class, messageCache.message);
        rmFieldRepublishProducer.republish(fwmtActionInstruction);
      } catch (GatewayException e) {
        //TODO
      }
    }
    if (messageCache.messageType.equals("CANCEL(HELD)")) {
      cacheService.save(gatewayCache.toBuilder().lastActionInstruction("CANCEL")
          .lastActionTime(messageQueueTime).build());
    }

  }
}