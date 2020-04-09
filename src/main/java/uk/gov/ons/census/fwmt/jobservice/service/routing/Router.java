package uk.gov.ons.census.fwmt.jobservice.service.routing;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtSuperInstruction;
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

public interface Router<I extends FwmtSuperInstruction, O> {
  default O route(I ffu, GatewayCache cache, GatewayEventManager eventManager) throws GatewayException {
    if (isValid(ffu, cache)) {
      return routeUnsafe(ffu, cache);
    } else {
      String ffuDetail = ffu.toRoutingString();
      String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
      String msg = this.getClass().getSimpleName() + " is unable to route the following message: " +
          ffuDetail + " with " + cacheDetail;
      eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
    }
  }

  // skip initial checks
  O routeUnsafe(I ffu, GatewayCache cache) throws GatewayException;

  Boolean isValid(I ffu, GatewayCache cache);
}
