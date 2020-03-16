package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Update")
@Service
public class SpgUpdateSiteRouter implements Router<CaseReopenCreateRequest> {
  @Override
  public CaseReopenCreateRequest routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgUpdateConverter.convertSite(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgUpdateRouter
      return ffu.getAddressLevel().equals("E")
          && cache.existsInFwmt;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
