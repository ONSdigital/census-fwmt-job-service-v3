package uk.gov.ons.census.fwmt.jobservice.converter;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

public interface CometConverter {

  CaseRequest convert(FieldworkFollowup ingest) throws GatewayException;

  Boolean isValid(FieldworkFollowup ffu);
}