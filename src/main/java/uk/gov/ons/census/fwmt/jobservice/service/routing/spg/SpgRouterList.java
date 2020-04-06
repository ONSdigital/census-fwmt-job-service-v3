package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;

@Component
public class SpgRouterList extends RouterList<SpgRouter> {
  public SpgRouterList(GatewayEventManager eventManager, List<SpgRouter> routers) {
    super(eventManager);
    setRouters(routers);
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    super.route(ffu, cache);
  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter
      return ffu.getSurveyName().equals("CENSUS")
          && ffu.getAddressType().equals("SPG");
    } catch (NullPointerException e) {
      return false;
    }
  }
}
