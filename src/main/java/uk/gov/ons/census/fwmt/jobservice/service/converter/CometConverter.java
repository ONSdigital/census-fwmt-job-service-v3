package uk.gov.ons.census.fwmt.jobservice.service.converter;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public interface CometConverter<T> {
  T convert(FieldworkFollowup ingest, GatewayCache cache) throws GatewayException;

  Boolean isValid(FieldworkFollowup ffu, GatewayCache cache);
}
