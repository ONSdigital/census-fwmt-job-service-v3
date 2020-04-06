package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.cancel;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CANCEL_TM_JOB;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

@Component
public class CancelRequestSender {

  private final RoutingValidator routingValidator;
  private final CometRestClient cometRestClient;
  private final GatewayEventManager eventManager;

  public CancelRequestSender(RoutingValidator routingValidator,
      CometRestClient cometRestClient,
      GatewayEventManager eventManager) {
    this.routingValidator = routingValidator;
    this.cometRestClient = cometRestClient;
    this.eventManager = eventManager;
  }

  public void send(CaseCreateRequest request, FieldworkFollowup ffu) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CANCEL_SENT, "Case Ref", ffu.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendDelete(ffu.getCaseId());

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Cancel", FAILED_TO_CANCEL_TM_JOB);
    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CANCEL_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());


  }

}
