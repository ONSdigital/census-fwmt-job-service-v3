package uk.gov.ons.census.fwmt.jobservice.service.routing.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.CaseType;
import uk.gov.ons.census.fwmt.jobservice.data.ConvertCachedMessage;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.sql.Timestamp;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_CREATE;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB;

@Slf4j
@Service
public class CeUpdateCommonProcessor {

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
  private CeUpdateCommonProcessor ceUpdateCommonProcessor;

  @Autowired
  private ConvertCachedMessage convertCachedMessage;

  @Autowired
  private CeCreateCommonProcessor ceCreateCommonProcessor;

  public void commonProcessor(FwmtActionInstruction rmRequest, CeCasePatchRequest tmRequest) throws GatewayException {
    Timestamp lastActionInstructionTime = new Timestamp(System.currentTimeMillis());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendCeDetails(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Update", FAILED_TO_UPDATE_TM_JOB);

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_ACK,
            "Case Ref", rmRequest.getCaseRef(),
            "Response Code", response.getStatusCode().name());

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());
    cacheService.save(newCache.toBuilder().lastActionInstruction(CaseType.UPDATE.toString())
        .lastActionTime(lastActionInstructionTime).build());
  }

  public void processPreUpdate(FwmtActionInstruction rmRequest, String converterMethod, GatewayCache cache) throws GatewayException {
    CeCasePatchRequest tmRequest;
    MessageCache messageCache = messageCacheService.getByIdAndMessageType(rmRequest.getCaseId(), "Update");
    tmRequest = convertCachedMessage.convertMessageToDTO(CeCasePatchRequest.class, messageCache.message);
    ceCreateCommonProcessor.commonProcessor(rmRequest, converterMethod, cache, 1, false);
    ceUpdateCommonProcessor.commonProcessor(rmRequest, tmRequest);
  }
}
