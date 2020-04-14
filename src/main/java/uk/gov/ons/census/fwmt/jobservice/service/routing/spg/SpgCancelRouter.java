package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Qualifier("SPG")
@Service
public class SpgCancelRouter implements Router<FwmtCancelActionInstruction, Void> {
  private final RouterList<FwmtCancelActionInstruction, ResponseEntity<Void>> router;
  private final RoutingValidator routingValidator;
  private final GatewayEventManager eventManager;

  public SpgCancelRouter(RoutingValidator routingValidator, CometRestClient cometRestClient,
      GatewayEventManager eventManager) {
    this.router = new RouterList<>(List.of(
        new SpgCancelSiteRouter(cometRestClient),
        new SpgCancelUnitRouter(cometRestClient)
    ), eventManager);
    this.routingValidator = routingValidator;
    this.eventManager = eventManager;
  }

  @Override
  public Void routeUnsafe(FwmtCancelActionInstruction ffu, GatewayCache cache) throws GatewayException {
    // TODO is this even right? It's saying it's sent before it runs the send - in all code
    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CANCEL_SENT, "Case Ref", "N/A");

    ResponseEntity<Void> response = router.route(ffu, cache, eventManager);

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Cancel", FAILED_TO_CREATE_TM_JOB);

    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CANCEL_ACK, "Case Ref", "N/A", "Response Code",
            response.getStatusCode().name());
    return null;
  }

  @Override
  public Boolean isValid(FwmtCancelActionInstruction ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter
      return ffu.getActionInstruction() == ActionInstructionType.CANCEL
          && ffu.getSurveyName().equals("CENSUS")
          && ffu.getAddressType().equals("SPG")
          && router.isValid(ffu, cache);
    } catch (NullPointerException e) {
      return false;
    }
  }
}
