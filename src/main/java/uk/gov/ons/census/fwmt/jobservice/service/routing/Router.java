package uk.gov.ons.census.fwmt.jobservice.service.routing;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public interface Router<T> {
  default T route(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    if (isValid(ffu, cache)) {
      return routeUnsafe(ffu, cache);
    }
    // TODO raise exception
    return null;
  }

  // skip initial checks
  T routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException;

  Boolean isValid(FieldworkFollowup ffu, GatewayCache cache);
}
