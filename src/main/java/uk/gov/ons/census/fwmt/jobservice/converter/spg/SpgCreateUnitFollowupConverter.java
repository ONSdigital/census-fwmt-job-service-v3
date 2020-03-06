package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;

@Component
public class SpgCreateUnitFollowupConverter implements CometConverter {

  private final SpgFollowUpSchedulingService followUpService;

  public SpgCreateUnitFollowupConverter(SpgFollowUpSchedulingService spgFollowUpSchedulingService) {
    this.followUpService = spgFollowUpSchedulingService;
  }

  @Override
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) {
    return out.surveyType(CaseRequest.SurveyType.SPG_Unit_F);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    try {
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("U")
          && (!ffu.getHandDeliver() || (followUpService.isInFollowUp() && gco.delivered));
    } catch (NullPointerException e) {
      return false;
    }
  }
}
