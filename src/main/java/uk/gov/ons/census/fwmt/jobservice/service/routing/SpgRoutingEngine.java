package uk.gov.ons.census.fwmt.jobservice.service.routing;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgRouterList;

@Component
public class SpgRoutingEngine {
  private final SpgRouterList routerList;
  private final GatewayEventManager eventManager;

  public SpgRoutingEngine(GatewayEventManager eventManager, SpgRouterList routerList) {
    this.eventManager = eventManager;
    this.routerList = routerList;
  }

  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    if (routerList.isValid(ffu, cache)){
      routerList.route(ffu, cache);
    }else {
      noRouter(ffu, cache);
    }
  }
  private GatewayException noRouter(FieldworkFollowup ffu, GatewayCache cache) {
    String ffuDetail = ffu.toRoutingString();
    String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
    String msg = this.getClass().getSimpleName() + " is unable to route the following message: " +
        ffuDetail + " with " + cacheDetail;
    eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), GatewayEventsConfig.ROUTING_FAILED);
    return new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
  }
}
