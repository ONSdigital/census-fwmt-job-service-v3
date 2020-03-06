package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

@Component
public class SpgCreateUnitDeliverConverter implements CometConverter {
  @Override
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) {
    return out.surveyType(CaseRequest.SurveyType.SPG_Unit_D);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    try {
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("U")
          && ffu.getHandDeliver()
          && !gco.existsInFwmt
          && !gco.delivered;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
