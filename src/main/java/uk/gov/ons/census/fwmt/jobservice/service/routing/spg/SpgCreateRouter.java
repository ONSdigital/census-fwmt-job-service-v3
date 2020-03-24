package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("SPG")
@Service
public class SpgCreateRouter implements Router<Void> {
  private final RouterList<CaseCreateRequest> router;
  private final RoutingValidator routingValidator;
  private final CometRestClient cometRestClient;
  private final GatewayEventManager eventManager;
  private final GatewayCacheService cacheService;

  public SpgCreateRouter(RoutingValidator routingValidator, CometRestClient cometRestClient,
      GatewayEventManager eventManager, GatewayCacheService cacheService,
      SpgFollowUpSchedulingService schedulingService) {
    this.router = new RouterList<>(List.of(
        new SpgCreateSiteRouter(),
        new SpgCreateSecureSiteRouter(),
        new SpgCreateUnitDeliverRouter(),
        new SpgCreateUnitFollowupRouter(schedulingService)
    ), eventManager);
    this.routingValidator = routingValidator;
    this.cometRestClient = cometRestClient;
    this.eventManager = eventManager;
    this.cacheService = cacheService;
  }

  @Override
  public Void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    CaseCreateRequest request = router.route(ffu, cache, eventManager);

    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CREATE_SENT, "Case Ref", ffu.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendCreate(request, ffu.getCaseId());

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);

    // Save the new cache object
    GatewayCache newCache = cacheService.getById(ffu.getCaseId());
    // update or create the cache entry with existsInFwmt=true
    if (newCache == null) {
      cacheService.save(GatewayCache.builder().caseId(ffu.getCaseId()).existsInFwmt(true).build());
    } else {
      cacheService.save(newCache.toBuilder().existsInFwmt(true).build());
    }

    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CREATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());
    return null;
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("CENSUS")
          && ffu.getAddressType().equals("SPG")
          && router.isValid(ffu, cache);
    } catch (NullPointerException e) {
      return false;
    }
  }
}
