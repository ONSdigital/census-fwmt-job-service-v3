package uk.gov.ons.census.fwmt.jobservice.converter;

import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

public interface CometConverter {
  ModelCase convert(CreateFieldWorkerJobRequest ingest) throws GatewayException;
}