package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.cancel;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

@Component
public class SpgCancelSiteRouter implements SPGCancelRouter {
  private final CometRestClient cometRestClient;
  private final GatewayEventManager eventManager;
  private final RoutingValidator routingValidator;


  public SpgCancelSiteRouter(CometRestClient cometRestClient, GatewayEventManager eventManager, RoutingValidator routingValidator) {
    this.cometRestClient = cometRestClient;
    this.eventManager = eventManager;
    this.routingValidator = routingValidator;
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException{
    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CANCEL_SENT, "Case Ref", ffu.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendDelete(ffu.getCaseId());

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Cancel", FAILED_TO_CREATE_TM_JOB);
    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CANCEL_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());
}

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      return ffu.getAddressLevel().equals("E")
          && cache != null;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
