package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.gatewaycache.GatewayCache;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;

@Qualifier("SPG")
public class SpgCreateSiteConverter implements CometConverter {
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) throws GatewayException {
    return out.surveyType(CaseRequest.SurveyType.SPG_Site);
  }

  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    // TODO this is existsInFwmt, not existsInField
    return ffu.getAddressLevel().equals("E") && !ffu.getSecureEstablishment() && (gco == null || !gco.existsInFwmt);
  }
}
