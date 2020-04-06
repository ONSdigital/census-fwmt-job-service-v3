package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.create;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;

@Qualifier("SPG Create")
@Service
public class SpgCreateSecureSiteRouter implements SpgCreateRouter {
  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    // TODO this is existsInFwmt, not existsInField
    //try {
    //// relies on the validation of: SpgRouter, SpgCreateRouter
    //  return ffu.getAddressLevel().equals("E")
    //      && ffu.getSecureEstablishment()
    //      && !cache.existsInFwmt;
    //} catch (NullPointerException e) {
    //  return false;
    //}
    // For the time being, this is disabled, and thus false.
    return false;
  }

  @Override
  public void route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    SpgCreateConverter.convertSecureSite(ffu, cache);

  }
}
