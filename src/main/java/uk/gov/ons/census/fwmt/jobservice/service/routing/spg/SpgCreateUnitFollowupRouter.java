package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Create")
public class SpgCreateUnitFollowupRouter implements Router<CaseCreateRequest> {

  private final SpgFollowUpSchedulingService followUpService;

  public SpgCreateUnitFollowupRouter(SpgFollowUpSchedulingService spgFollowUpSchedulingService) {
    this.followUpService = spgFollowUpSchedulingService;
  }

  @Override
  public CaseCreateRequest routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgCreateConverter.convertUnitFollowup(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("U")
          && (!ffu.getHandDeliver() || (followUpService.isInFollowUp() && cache.delivered));
    } catch (NullPointerException e) {
      return false;
    }
  }
}
