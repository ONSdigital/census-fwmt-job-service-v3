package uk.gov.ons.census.fwmt.jobservice.service.routing;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;
import java.util.Optional;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

public class RouterList<T> implements Router<T> {
  public final List<Router<T>> routers;
  private final GatewayEventManager eventManager;

  public RouterList(List<Router<T>> routers, GatewayEventManager eventManager) {
    this.routers = routers;
    this.eventManager = eventManager;
  }

  @Override
  public T routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return getRouter(ffu, cache).route(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    return routers.stream().anyMatch(s -> s.isValid(ffu, cache));
  }

  public Optional<Router<T>> maybeGetRouter(FieldworkFollowup ffu, GatewayCache cache) {
    return routers.stream().filter(s -> s.isValid(ffu, cache)).findFirst();
  }

  public Router<T> getRouter(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return maybeGetRouter(ffu, cache).orElseThrow(() -> noRouter(ffu, cache));
  }

  private GatewayException noRouter(FieldworkFollowup ffu, GatewayCache cache) {
    String ffMsg = "FieldworkFollowup(actionInstruction=%s, surveyName=%s, addressType=%s, addressLevel=%s, secureEstablishment=%s)";
    String gcMsg = "GatewayCache(caseId, existsInField, delivered)";
    String msg = "No router found for " + ffMsg + " and " + gcMsg;
    eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), ROUTING_FAILED);
    return new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
  }
}
