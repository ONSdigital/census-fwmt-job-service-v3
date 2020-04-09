package uk.gov.ons.census.fwmt.jobservice.service.routing;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtSuperInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;
import java.util.Optional;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

public class RouterList<I extends FwmtSuperInstruction, O> implements Router<I, O> {
  public final List<Router<I, O>> routers;
  private final GatewayEventManager eventManager;

  public RouterList(List<Router<I, O>> routers, GatewayEventManager eventManager) {
    this.routers = routers;
    this.eventManager = eventManager;
  }

  public O route(I ffu, GatewayCache cache) throws GatewayException {
    return getRouter(ffu, cache).route(ffu, cache, eventManager);
  }

  @Override
  public O routeUnsafe(I ffu, GatewayCache cache) throws GatewayException {
    return getRouter(ffu, cache).routeUnsafe(ffu, cache);
  }

  @Override
  public Boolean isValid(I ffu, GatewayCache cache) {
    return routers.stream().anyMatch(s -> s.isValid(ffu, cache));
  }

  public Optional<Router<I, O>> maybeGetRouter(I ffu, GatewayCache cache) {
    return routers.stream().filter(s -> s.isValid(ffu, cache)).findFirst();
  }

  public Router<I, O> getRouter(I ffu, GatewayCache cache) throws GatewayException {
    return maybeGetRouter(ffu, cache).orElseThrow(() -> noRouter(ffu, cache));
  }

  private GatewayException noRouter(I ffu, GatewayCache cache) {
    String ffuDetail = ffu.toRoutingString();
    String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
    String msg = this.getClass().getSimpleName() + " is unable to route the following message: " +
        ffuDetail + " with " + cacheDetail;
    eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), ROUTING_FAILED);
    return new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
  }
}
