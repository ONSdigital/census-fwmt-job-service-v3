package uk.gov.ons.census.fwmt.jobservice.converter;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.gatewaycache.GatewayCache;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

public interface CometConverter {
  CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache cache, CaseRequest.CaseRequestBuilder out) throws GatewayException;

  Boolean isValid(FieldworkFollowup ffu, GatewayCache cache);
}
