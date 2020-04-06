package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.create;

import org.springframework.stereotype.Service;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;

@Service
public class SpgCreateUnitFollowupRouter implements SpgCreateRouter {
  private final CreateRequestSender sender;

  private final SpgFollowUpSchedulingService followUpService;

  public SpgCreateUnitFollowupRouter(CreateRequestSender sender, SpgFollowUpSchedulingService spgFollowUpSchedulingService) {
    this.sender = sender;
    this.followUpService = spgFollowUpSchedulingService;
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
     CaseCreateRequest caseCreateRequest = SpgCreateConverter.convertUnitFollowup(ffu, cache);
     sender.send(caseCreateRequest, ffu);
  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgCreateRouter
      return ffu.getAddressLevel().equals("U")
          && (!ffu.getHandDeliver() || (followUpService.isInFollowUp() && cache.delivered));
    } catch (NullPointerException e) {
      return false;
    }
  }
}
