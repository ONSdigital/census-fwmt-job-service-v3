package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.create;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;

@Component
public class SpgCreateSiteRouter implements SpgCreateRouter {
  private final CreateRequestSender sender;

  public SpgCreateSiteRouter( CreateRequestSender sender) {
    this.sender = sender;
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    CaseCreateRequest caseCreateRequest = SpgCreateConverter.convertSite(ffu, cache);
    sender.send(caseCreateRequest, ffu);
  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    // TODO this is existsInFwmt, not existsInField
    try {
      // relies on the validation of: SpgRouter, SpgCreateRouter
      return ffu.getAddressLevel().equals("E")
          && !ffu.getSecureEstablishment()
          && (cache == null || !cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }
}
