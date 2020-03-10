package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Update")
public class SpgUpdateUnitRouter implements Router<CaseReopenCreateRequest> {
  private final SpgCreateRouter createRouter;

  public SpgUpdateUnitRouter(SpgCreateRouter createRouter) {
    this.createRouter = createRouter;
  }

  @Override
  public CaseReopenCreateRequest routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    if (ffu.getUaa() && cache == null) {
      // re-run as CREATE
      // TODO alter object
      createRouter.route(ffu, null);
    }

    return SpgUpdateConverter.convertUnit(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    try {
      return ffu.getActionInstruction().equals("UPDATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("U")
          && (ffu.getUaa() || ffu.getBlankQreReturned())
          && gco.existsInFwmt;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
