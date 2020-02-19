package uk.gov.ons.census.fwmt.jobservice.comet.converter.spg;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.comet.converter.CometConverter;

@Component
public class SpgSiteConverter implements CometConverter {

  @Override
  public CaseRequest convert(FieldworkFollowup ingest) throws GatewayException {
    return SpgMapper.map(ingest);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu) {
    try {
      if (!ffu.getActionInstruction().equals("CREATE")) return false;
      if (!ffu.getSurveyName().equals("Census")) return false;
      if (!ffu.getAddressType().equals("SPG")) return false;
      if (!ffu.getAddressLevel().equals("E")) return false;
      if (ffu.getSecureEstablishment()) return false;

      return true;
    } catch (Exception e) {
      return false;
    }
  }

}
