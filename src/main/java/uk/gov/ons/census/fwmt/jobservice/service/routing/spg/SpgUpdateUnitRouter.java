package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Update")
@Service
public class SpgUpdateUnitRouter implements Router<CaseReopenCreateRequest> {
  private final SpgCreateRouter createRouter;
  private final GatewayEventManager eventManager;

  public SpgUpdateUnitRouter(SpgCreateRouter createRouter, GatewayEventManager eventManager) {
    this.createRouter = createRouter;
    this.eventManager = eventManager;
  }

  @Override
  public CaseReopenCreateRequest routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    if (ffu.getUaa() && cache == null) {
      // re-run as CREATE
      createRouter.route(ffu.toBuilder().actionInstruction("CREATE").build(), null, eventManager);
      return null;
    }

    return SpgUpdateConverter.convertUnit(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgUpdateRouter
      return ffu.getAddressLevel().equals("U")
          && (ffu.getUaa() || ffu.getBlankQreReturned())
          && cache.existsInFwmt;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
