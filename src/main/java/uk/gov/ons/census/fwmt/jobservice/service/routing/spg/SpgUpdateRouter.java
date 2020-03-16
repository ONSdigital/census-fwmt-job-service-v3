package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB;

@Qualifier("SPG")
@Service
public class SpgUpdateRouter implements Router<Void> {
  private final RouterList<CaseReopenCreateRequest> router;
  private final RoutingValidator routingValidator;
  private final CometRestClient cometRestClient;
  private final GatewayEventManager eventManager;

  public SpgUpdateRouter(RoutingValidator routingValidator, CometRestClient cometRestClient,
      GatewayEventManager eventManager, SpgCreateRouter createRouter) {
    this.router = new RouterList<>(List.of(
        new SpgUpdateUnitRouter(createRouter, eventManager),
        new SpgUpdateSiteRouter()
    ), eventManager);
    this.routingValidator = routingValidator;
    this.cometRestClient = cometRestClient;
    this.eventManager = eventManager;
  }

  @Override
  public Void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    CaseReopenCreateRequest request = router.route(ffu, cache, eventManager);

    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_UPDATE_SENT, "Case Ref", ffu.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendRequest(request, ffu.getCaseId());

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Update", FAILED_TO_UPDATE_TM_JOB);

    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_UPDATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());

    return null;
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter
      return ffu.getActionInstruction().equals("UPDATE")
          && ffu.getSurveyName().equals("CENSUS")
          && ffu.getAddressType().equals("SPG")
          && router.isValid(ffu, cache);
    } catch (NullPointerException e) {
      return false;
    }
  }
}
