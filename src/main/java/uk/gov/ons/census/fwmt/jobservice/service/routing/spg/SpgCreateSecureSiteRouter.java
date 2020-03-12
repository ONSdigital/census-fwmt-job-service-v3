package uk.gov.ons.census.fwmt.jobservice.service.routing.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.Router;

@Qualifier("SPG Create")
public class SpgCreateSecureSiteRouter implements Router<CaseCreateRequest> {
  @Override
  public CaseCreateRequest routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgCreateConverter.convertSecureSite(ffu, cache);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    // TODO this is existsInFwmt, not existsInField
    //try {
    //  return ffu.getActionInstruction().equals("CREATE")
    //      && ffu.getSurveyName().equals("Census")
    //      && ffu.getAddressType().equals("SPG")
    //      && ffu.getAddressLevel().equals("E")
    //      && ffu.getSecureEstablishment()
    //      && !cache.existsInFwmt;
    //} catch (NullPointerException e) {
    //  return false;
    //}
    // For the time being, this is disabled, and thus false.
    return false;
  }
}
