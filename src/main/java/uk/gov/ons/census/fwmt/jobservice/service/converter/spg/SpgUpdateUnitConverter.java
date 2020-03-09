package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;

@Component
public class SpgUpdateUnitConverter implements CometConverter {
  @Override
  public CaseRequest convert(
      FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgUpdateCommon.convert(ffu, cache, CaseRequest.builder()).build();
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
