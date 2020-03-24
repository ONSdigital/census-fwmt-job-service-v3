package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Cancel")
@Service
public class SpgCancelSiteRouter implements Router<ResponseEntity<Void>> {
  private final CometRestClient cometRestClient;

  public SpgCancelSiteRouter(CometRestClient cometRestClient) {
    this.cometRestClient = cometRestClient;
  }

  @Override
  public ResponseEntity<Void> routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return cometRestClient.sendDelete(ffu.getCaseId());
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter, SpgCancelRouter
      return ffu.getAddressLevel().equals("E")
          // TODO this wasn't in the spec
          && cache != null;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
