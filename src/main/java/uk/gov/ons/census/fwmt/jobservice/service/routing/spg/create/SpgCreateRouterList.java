package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.create;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgRouter;

@Component
public class SpgCreateRouterList extends RouterList<SpgCreateRouter> implements SpgRouter{
  public SpgCreateRouterList(GatewayEventManager eventManager, List<SpgCreateRouter> routers) {
    super(eventManager);
    setRouters(routers);
  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("CENSUS")
          && ffu.getAddressType().equals("SPG");
    } catch (NullPointerException e) {
      return false;
    }
  }

}
