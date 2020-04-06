package uk.gov.ons.census.fwmt.jobservice.service.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public abstract class RouterList<T extends Router> implements Router{//implements Router<T> {
  private List<T> routers;
  private final GatewayEventManager eventManager;

  public RouterList(GatewayEventManager eventManager) {
    this.eventManager = eventManager;
  }

  public void setRouters(List<T> routers){
    this.routers = new ArrayList<T>();
    this.routers.addAll(routers);
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    Optional<T> oRouter = getValidRouter(ffu, cache);
    if (oRouter.isPresent()) {
      oRouter.get().route(ffu, cache);
    }else {
      noRouter(ffu, cache);
    }
  }

//  @Override
//  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
//    return routers.stream().anyMatch(s -> s.isValid(ffu));
//  }

  private Optional<T> getValidRouter(FieldworkFollowup ffu, GatewayCache cache){
    return routers.stream().filter(s -> s.isValid(ffu, cache)).findFirst();
  }

  private GatewayException noRouter(FieldworkFollowup ffu, GatewayCache cache) {
    String ffuDetail = ffu.toRoutingString();
    String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
    String msg = this.getClass().getSimpleName() + " is unable to route the following message: " +
        ffuDetail + " with " + cacheDetail;
    eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), GatewayEventsConfig.ROUTING_FAILED);
    return new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
  }

  //
//  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
//    getRouter(ffu, cache).route(ffu, cache, eventManager);
//  }
//
//  @Override
//  public void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
//     getRouter(ffu, cache).routeUnsafe(ffu, cache);
//  }
//
//  @Override
//  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
//    return routers.stream().anyMatch(s -> s.isValid(ffu, cache));
//  }
//
//  public Optional<T> maybeGetRouter(FieldworkFollowup ffu, GatewayCache cache) {
//    return routers.stream().filter(s -> s.isValid(ffu, cache)).findFirst();
//  }
//
//  public T getRouter(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
//    return maybeGetRouter(ffu, cache).orElseThrow(() -> noRouter(ffu, cache));
//  }
//

}
