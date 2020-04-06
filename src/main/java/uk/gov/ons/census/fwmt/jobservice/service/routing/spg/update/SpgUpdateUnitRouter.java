package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.update;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.create.SpgCreateRouterList;

@Component
public class SpgUpdateUnitRouter implements SpgUpdateRouter {
  private final SpgCreateRouterList createRouterList;
  private final UpdateRequestSender sender;

  public SpgUpdateUnitRouter(SpgCreateRouterList createRouterList, UpdateRequestSender sender) {
    this.createRouterList = createRouterList;
    this.sender = sender;
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    if (ffu.getUaa() && cache == null) {
      // re-run as CREATE
      createRouterList.route(ffu.toBuilder().actionInstruction("CREATE").build(), null);
    } else {
      CaseReopenCreateRequest caseReopenCreateRequest = SpgUpdateConverter.convertUnit(ffu, cache);
      sender.send(caseReopenCreateRequest, ffu);
    }
  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgUpdateRouter
      return ffu.getAddressLevel().equals("U")
          && (ffu.getUaa() || (ffu.getBlankQreReturned() && cache.existsInFwmt));
    } catch (NullPointerException e) {
      return false;
    }
  }
}
