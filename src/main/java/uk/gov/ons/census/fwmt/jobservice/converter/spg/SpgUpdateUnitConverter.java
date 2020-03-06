package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

@Component
public class SpgUpdateUnitConverter implements CometConverter {
  @Override
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) {
    return out;
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    try {
      return ffu.getActionInstruction().equals("UPDATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("U")
          && (ffu.getUaa() || ffu.getBlankQreReturned())
          && gco.existsInFwmt;
    } catch (NullPointerException e) {
      return false;
    }
  }
}
