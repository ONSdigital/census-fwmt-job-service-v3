package uk.gov.ons.census.fwmt.jobservice.service.routing;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

/**
 * Current routing structure:
 * - SpgRouter:
 * -   SpgCreateRouter
 * -     SpgCreateSiteRouter
 * -     SpgCreateSecureSiteRouter
 * -     SpgCreateUnitDeliverRouter
 * -     SpgCreateUnitFollowupRouter
 * -   SpgUpdateRouter
 * -     SpgUpdateSiteRouter
 * -     SpgUpdateUnitRouter
 *
 * @param <T>
 */

public interface Router<T> {
  default T route(FieldworkFollowup ffu, GatewayCache cache, GatewayEventManager eventManager) throws GatewayException {
    if (isValid(ffu, cache)) {
      return routeUnsafe(ffu, cache);
    } else {
      String ffuDetail = ffu.toRoutingString();
      String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
      String msg = this.getClass().getName() + " is unable to route the following message: " +
          ffuDetail + " with " + cacheDetail;
      eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
    }
  }

  // skip initial checks
  T routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException;

  Boolean isValid(FieldworkFollowup ffu, GatewayCache cache);
}
