package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Create")
@Service
public class SpgCreateUnitFollowupRouter implements Router<FwmtActionInstruction, CaseCreateRequest> {

  private final SpgFollowUpSchedulingService followUpService;

  public SpgCreateUnitFollowupRouter(SpgFollowUpSchedulingService spgFollowUpSchedulingService) {
    this.followUpService = spgFollowUpSchedulingService;
  }

  @Override
  public CaseCreateRequest routeUnsafe(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    return SpgCreateConverter.convertUnitFollowup(ffu, cache);
  }

  @Override
  public Boolean isValid(FwmtActionInstruction ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgCreateRouter
      return ffu.getAddressLevel().equals("U");
    } catch (NullPointerException e) {
      return false;
    }
  }
}
