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

  public void commonProcessor(FwmtActionInstruction rmRequest, GatewayCache cache, int type, boolean isFollowUp) throws GatewayException {
    CaseRequest tmRequest;

    Timestamp lastActionInstructionTime = new Timestamp(System.currentTimeMillis());

    switch(type) {
      case 1:
        if (isFollowUp) {
          if (rmRequest.isSecureEstablishment()) {
            tmRequest = CeCreateConverter.convertCeEstabFollowupSecure(rmRequest, cache);
          } else {
            tmRequest = CeCreateConverter.convertCeEstabFollowup(rmRequest, cache);
          }
        } else {
          if (rmRequest.isSecureEstablishment()) {
            tmRequest = CeCreateConverter.convertCeEstabDeliverSecure(rmRequest, cache);
          } else {
            tmRequest = CeCreateConverter.convertCeEstabDeliver(rmRequest, cache);
          }
        }
        break;
      case 2:
        if (rmRequest.isSecureEstablishment()) {
          tmRequest = CeCreateConverter.convertCeSiteSecure(rmRequest, cache);
        } else {
          tmRequest = CeCreateConverter.convertCeSite(rmRequest, cache);
        }
        break;
      case 3:
        if (isFollowUp) {
          if (rmRequest.isSecureEstablishment()) {
            tmRequest = CeCreateConverter.convertCeUnitFollowupSecure(rmRequest, cache);
          } else {
            tmRequest = CeCreateConverter.convertCeUnitFollowup(rmRequest, cache);
          }
        } else {
          if (rmRequest.isSecureEstablishment()) {
            tmRequest = CeCreateConverter.convertCeUnitDeliverSecure(rmRequest, cache);
          } else {
            tmRequest = CeCreateConverter.convertCeUnitDeliver(rmRequest, cache);
          }
        }
        break;
      default:
        throw new NotImplementedException("Type does not match gateway types");
    }

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_PRE_SENDING, "Case Ref",
        tmRequest.getReference(), "Survey Type",
        tmRequest.getSurveyType().toString());

    ResponseEntity<Void> response = cometRestClient.sendCreate(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);

    GatewayCache newCache = cacheService.getById(rmRequest.getCaseId());

    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
          .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(type).lastActionInstruction("Create")
          .lastActionTime(lastActionInstructionTime).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).lastActionInstruction("Create")
          .lastActionTime(lastActionInstructionTime).build());
    }

    messageCacheService.save(MessageCache.builder().caseId(rmRequest.getCaseId()).messageType("Create").build());

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CREATE_ACK, "Case Ref", rmRequest.getCaseRef(),
            "Response Code",
            response.getStatusCode().name(), "Survey Type", tmRequest.getSurveyType().toString());

    if (messageCacheService.doesCaseIdAndMessageTypeExist(rmRequest.getCaseId(), "Update")) {
      preCreateUpdate();
    }
  }

  public void preCreateCancel(FwmtActionInstruction rmRequest, int type) {
    Timestamp lastActionInstructionTime = new Timestamp(System.currentTimeMillis());

    cacheService.save(GatewayCache.builder().caseId(rmRequest.getCaseId()).existsInFwmt(true)
        .uprn(rmRequest.getUprn()).estabUprn(rmRequest.getEstabUprn()).type(type).lastActionInstruction("Cancel")
        .lastActionTime(lastActionInstructionTime).build());

    messageCacheService.delete(MessageCache.builder().caseId(rmRequest.getCaseId()).build());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CANCEL_CREATE,
        "Case Ref", "N/A",
        "TM Action", "Cancel already present for create. Create cancelled and not sent to TM");
  }

  public void preCreateUpdate(){

  }
}
