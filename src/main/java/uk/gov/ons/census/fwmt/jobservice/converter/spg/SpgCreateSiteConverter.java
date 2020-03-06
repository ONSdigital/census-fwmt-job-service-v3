package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

@Component
@Qualifier("SPG")
public class SpgCreateSiteConverter implements CometConverter {
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) {
    return out.surveyType(CaseRequest.SurveyType.SPG_Site);
  }

  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    // TODO this is existsInFwmt, not existsInField
    return ffu.getAddressLevel().equals("E") && !ffu.getSecureEstablishment() && (gco == null || !gco.existsInFwmt);
  }
}
