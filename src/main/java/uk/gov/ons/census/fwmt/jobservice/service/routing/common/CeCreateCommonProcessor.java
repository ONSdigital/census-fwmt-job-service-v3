package uk.gov.ons.census.fwmt.jobservice.service.routing.common;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.CaseType;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.lang.reflect.Method;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_CREATE;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Slf4j
@Service
public class CeCreateCommonProcessor {

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

  public void commonProcessor(FwmtActionInstruction rmRequest, String converterMethod, GatewayCache cache, int type, boolean isFollowUp)
      throws GatewayException {
    CaseRequest tmRequest;
    CeCreateConverter ceCreateConverter = null;
    Method converter;

    Timestamp lastActionInstructionTime = new Timestamp(System.currentTimeMillis());

    try {
      Class<?> ceCreate = Class.forName("uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeCreateConverter");
      converter = ceCreate.getDeclaredMethod(converterMethod, FwmtActionInstruction.class, GatewayCache.class );
      tmRequest = (CaseRequest) converter.invoke(ceCreateConverter, rmRequest, cache);
    } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      return;
    }


    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_PRE_SENDING, "Case Ref",
        tmRequest.getReference(), "Survey Type",
        tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());

    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
          .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(type)
          .lastActionInstruction(CaseType.CREATE.toString()).lastActionTime(lastActionInstructionTime).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).lastActionInstruction(CaseType.CREATE.toString())
          .lastActionTime(lastActionInstructionTime).build());
    }

    messageCacheService.save(MessageCache.builder().caseId(rmRequest.getCaseId()).messageType(CaseType.CREATE.toString()).build());

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_ACK, "Case Ref", rmRequest.getCaseRef(),
            "Response Code",
            response.getStatusCode().name(), "Survey Type", tmRequest.getSurveyType().toString());

  }

  public void preCreateCancel(FwmtActionInstruction rmRequest, int type) {
    Timestamp lastActionInstructionTime = new Timestamp(System.currentTimeMillis());

    cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
        .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(type).lastActionInstruction(CaseType.CANCEL.toString())
        .lastActionTime(lastActionInstructionTime).build());

    messageCacheService.delete(MessageCache.builder().caseId(rmRequest.getCaseId()).build());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_CREATE,
        "Case Ref", "N/A",
        "TM Action", "Cancel already present for create. Create cancelled and not sent to TM");
  }
}
