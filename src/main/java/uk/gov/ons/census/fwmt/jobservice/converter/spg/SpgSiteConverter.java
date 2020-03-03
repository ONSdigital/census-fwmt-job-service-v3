package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;

@Component
public class SpgSiteConverter implements CometConverter {

  @Override
  public CaseRequest convert(FieldworkFollowup ingest) throws GatewayException {
    return SpgMapper.map(ingest);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu) {
    try {
      return ffu.getActionInstruction().equals("CREATE")
          && ffu.getSurveyName().equals("Census")
          && ffu.getAddressType().equals("SPG")
          && ffu.getAddressLevel().equals("E")
          && !ffu.getSecureEstablishment();
    } catch (NullPointerException e) {
      return false;
    }
  }

}
