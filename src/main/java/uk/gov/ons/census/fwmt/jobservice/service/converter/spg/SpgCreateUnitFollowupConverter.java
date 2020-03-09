package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;

@Component
public class SpgCreateUnitFollowupConverter implements CometConverter<CaseRequest> {

  private final SpgFollowUpSchedulingService followUpService;

  public SpgCreateUnitFollowupConverter(SpgFollowUpSchedulingService spgFollowUpSchedulingService) {
    this.followUpService = spgFollowUpSchedulingService;
  }

  @Override
  public CaseRequest convert(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgCreateCommon.convert(ffu, cache, CaseRequest.builder())
        .surveyType(CaseRequest.SurveyType.SPG_Unit_F)
        .build();
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("U")
          && (!ffu.getHandDeliver() || (followUpService.isInFollowUp() && cache.delivered));
    } catch (NullPointerException e) {
      return false;
    }
  }
}
