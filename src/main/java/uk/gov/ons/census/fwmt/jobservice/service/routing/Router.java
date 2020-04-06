package uk.gov.ons.census.fwmt.jobservice.service.routing;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

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

public interface Router {
  void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException;

  boolean isValid(FieldworkFollowup ffu, GatewayCache cache);

//  default void route(FieldworkFollowup ffu, GatewayCache cache, GatewayEventManager eventManager) throws GatewayException {

    //    if (isValid(ffu, cache)) {
//      routeUnsafe(ffu, cache);
//    } else {
//      String ffuDetail = ffu.toRoutingString();
//      String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
//      String msg = this.getClass().getSimpleName() + " is unable to route the following message: " +
//          ffuDetail + " with " + cacheDetail;
//      eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), ROUTING_FAILED);
//      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
//    }
//  }
//
//  // skip initial checks
//  void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException;
//
//  Boolean isValid(FieldworkFollowup ffu, GatewayCache cache);
}
