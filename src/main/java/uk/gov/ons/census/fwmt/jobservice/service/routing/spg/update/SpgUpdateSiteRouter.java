package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.update;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;

@Component
public class SpgUpdateSiteRouter implements SpgUpdateRouter {

  private UpdateRequestSender sender;

  public SpgUpdateSiteRouter(UpdateRequestSender sender) {
    this.sender = sender;
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    CaseReopenCreateRequest caseReopenCreateRequest = SpgUpdateConverter.convertSite(ffu, cache);
    sender.send(caseReopenCreateRequest, ffu);
  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgUpdateRouter
      return ffu.getAddressLevel().equals("E")
          && cache.existsInFwmt;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
