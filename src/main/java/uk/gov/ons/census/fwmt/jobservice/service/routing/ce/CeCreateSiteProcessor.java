package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.ConvertCachedMessage;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
import uk.gov.ons.census.fwmt.jobservice.service.routing.common.CeCreateCommonProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.routing.common.CeUpdateCommonProcessor;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CREATE_FOR_CASE_ALREADY_EXISTS;

@Qualifier("Create")
@Service
public class CeCreateSiteProcessor implements InboundProcessor<FwmtActionInstruction> {

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private MessageCacheService messageCacheService;

  @Autowired
  private CeCreateCommonProcessor ceCreateCommonProcessor;

  @Autowired
  private CeUpdateCommonProcessor ceUpdateCommonProcessor;

  @Autowired
  private ConvertCachedMessage convertCachedMessage;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("E")
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
          && rmRequest.getAddressLevel().equals("E")
          && (cache == null
          || !cache.existsInFwmt)
          && cacheService.doesEstabUprnExist(rmRequest.getUprn());
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
    if (messageCacheService.doesCaseIdAndMessageTypeExist(rmRequest.getCaseId(), "Cancel")) {
      ceCreateCommonProcessor.preCreateCancel(rmRequest, 2);
    } else {
      if (messageCacheService.doesCaseIdAndMessageTypeExist(rmRequest.getCaseId(), "Create")) {
        eventManager.triggerErrorEvent(this.getClass(), "Create already exists for case",
            String.valueOf(rmRequest.getCaseId()), CREATE_FOR_CASE_ALREADY_EXISTS);
      } else if (messageCacheService.doesCaseIdAndMessageTypeExist(rmRequest.getCaseId(), "Update")) {
        CeCasePatchRequest tmRequest;
        MessageCache messageCache = messageCacheService.getByIdAndMessageType(rmRequest.getCaseId(), "Update");
        tmRequest = convertCachedMessage.convertMessageToDTO(CeCasePatchRequest.class, messageCache.message);
        ceCreateCommonProcessor.commonProcessor(rmRequest, cache, 2, false);
        ceUpdateCommonProcessor.commonProcessor(rmRequest, tmRequest);
      } else {
        ceCreateCommonProcessor.commonProcessor(rmRequest, cache, 2, false);
      }
    }
  }
}
