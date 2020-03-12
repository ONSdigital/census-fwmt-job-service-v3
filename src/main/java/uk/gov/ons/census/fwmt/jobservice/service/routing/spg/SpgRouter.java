package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;

import java.util.List;

@Primary
@Service
public class SpgRouter implements Router<Void> {
  private final RouterList<Void> router;

  public SpgRouter(SpgCreateRouter createRouter, SpgUpdateRouter updateRouter, GatewayEventManager eventManager) {
    this.router = new RouterList<>(List.of(createRouter, updateRouter), eventManager);
  }

  @Override
  public Void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return router.route(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    return router.isValid(ffu, cache);
  }
}
