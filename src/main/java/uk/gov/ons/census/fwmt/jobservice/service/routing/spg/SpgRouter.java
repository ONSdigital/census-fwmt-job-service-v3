package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.context.annotation.Primary;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Primary
public class SpgRouter implements Router<Void> {
  private final SpgCreateRouter createRouter;
  private final SpgUpdateRouter updateRouter;

  public SpgRouter(SpgCreateRouter createRouter, SpgUpdateRouter updateRouter) {
    this.createRouter = createRouter;
    this.updateRouter = updateRouter;
  }

  @Override
  public Void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    if (createRouter.isValid(ffu, cache)) {
      createRouter.route(ffu, cache);
    } else if (updateRouter.isValid(ffu, cache)) {
      updateRouter.route(ffu, cache);
    } else {
      // TODO error condition
    }

    return null;
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    return createRouter.isValid(ffu, cache) || updateRouter.isValid(ffu, cache);
  }
}
