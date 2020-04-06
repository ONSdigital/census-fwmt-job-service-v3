package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.create;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

@Component
public class CreateRequestSender {

  private final RoutingValidator routingValidator;
  private final CometRestClient cometRestClient;
  private final GatewayEventManager eventManager;
  private final GatewayCacheService cacheService;

  public CreateRequestSender(RoutingValidator routingValidator,
      CometRestClient cometRestClient,
      GatewayEventManager eventManager, GatewayCacheService cacheService) {
    this.routingValidator = routingValidator;
    this.cometRestClient = cometRestClient;
    this.eventManager = eventManager;
    this.cacheService = cacheService;
  }

  public void send(CaseCreateRequest request, FieldworkFollowup ffu) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), GatewayEventsConfig.COMET_CREATE_SENT, "Case Ref", ffu.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendCreate(request, ffu.getCaseId());

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Create", GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB);

    // Save the new cache object
    GatewayCache newCache = cacheService.getById(ffu.getCaseId());
    // update or create the cache entry with existsInFwmt=true
    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(ffu.getCaseId()).existsInFwmt(true).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).build());
    }

    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), GatewayEventsConfig.COMET_CREATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());

  }

}
