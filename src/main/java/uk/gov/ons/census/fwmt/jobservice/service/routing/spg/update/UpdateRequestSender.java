package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.update;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

@Component
public class UpdateRequestSender {

  private final RoutingValidator routingValidator;
  private final CometRestClient cometRestClient;
  private final GatewayEventManager eventManager;

  public UpdateRequestSender(RoutingValidator routingValidator,
      CometRestClient cometRestClient,
      GatewayEventManager eventManager) {
    this.routingValidator = routingValidator;
    this.cometRestClient = cometRestClient;
    this.eventManager = eventManager;
  }

  public void send(CaseReopenCreateRequest request, FieldworkFollowup ffu) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), GatewayEventsConfig.COMET_UPDATE_SENT, "Case Ref", ffu.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendReopen(request, ffu.getCaseId());

    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Update", GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB);

    eventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), GatewayEventsConfig.COMET_UPDATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());
  }

}
