package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

@Component
@Qualifier("SPG")
public class SpgCreateUnitDeliverConverter implements CometConverter {
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) {
    return out.surveyType(CaseRequest.SurveyType.SPG_Unit_D);
  }

  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    return ffu.getAddressLevel().equals("U") && ffu.getHandDeliver() && (gco != null) && !gco.existsInFwmt
        && !gco.delivered;
  }
}
