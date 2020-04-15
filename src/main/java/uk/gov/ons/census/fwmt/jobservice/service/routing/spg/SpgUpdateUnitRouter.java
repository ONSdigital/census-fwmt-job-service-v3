package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Update")
@Service
public class SpgUpdateUnitRouter implements Router<FwmtActionInstruction, CaseReopenCreateRequest> {
  private final SpgCreateRouter createRouter;
  private final GatewayEventManager eventManager;

  public SpgUpdateUnitRouter(SpgCreateRouter createRouter, GatewayEventManager eventManager) {
    this.createRouter = createRouter;
    this.eventManager = eventManager;
  }

  @Override
  public CaseReopenCreateRequest routeUnsafe(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    if (ffu.isUndeliveredAsAddress() && cache == null) {
      // re-run as CREATE
      createRouter.route(ffu.toBuilder().actionInstruction(ActionInstructionType.CREATE).build(), null, eventManager);
      return null;
    }

    return SpgUpdateConverter.convertUnit(ffu, cache);
  }

  @Override
  public Boolean isValid(FwmtActionInstruction ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgUpdateRouter
      return ffu.getAddressLevel().equals("U")
          && (ffu.isUndeliveredAsAddress() || cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }
}
