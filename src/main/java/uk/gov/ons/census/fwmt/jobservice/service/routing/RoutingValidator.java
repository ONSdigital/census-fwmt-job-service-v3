package uk.gov.ons.census.fwmt.jobservice.service.routing;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.util.List;

public class RoutingValidator {
  private final GatewayEventManager eventManager;

  public RoutingValidator(GatewayEventManager eventManager) {
    this.eventManager = eventManager;
  }

  private static final List<HttpStatus> validResponses = List
      .of(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.ACCEPTED);

  public void validateResponse(ResponseEntity<Void> response, String caseId, String verb, String errorCode)
      throws GatewayException {
    if (!validResponses.contains(response.getStatusCode())) {
      String code = response.getStatusCode().toString();
      String value = Integer.toString(response.getStatusCodeValue());
      String msg = "Unable to " + verb + " FieldWorkerJobRequest: HTTP_STATUS:" + code + ":" + value;
      eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(caseId), errorCode);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }
}
