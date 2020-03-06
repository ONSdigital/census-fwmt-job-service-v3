package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

@Component
public class SpgCreateSecureSiteConverter implements CometConverter {
  @Override
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ingest, GatewayCache gco, CaseRequest.CaseRequestBuilder out) {
    return out.surveyType(CaseRequest.SurveyType.SPG_Secure_Site);
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache gco) {
    // TODO this is existsInFwmt, not existsInField
    //try {
    //  return ffu.getActionInstruction().equals("CREATE")
    //      && ffu.getSurveyName().equals("Census")
    //      && ffu.getAddressType().equals("SPG")
    //      && ffu.getAddressLevel().equals("E")
    //      && ffu.getSecureEstablishment()
    //      && !gco.existsInFwmt;
    //} catch (NullPointerException e) {
    //  return false;
    //}
    // For the time being, this is disabled, and thus false.
    return false;
  }
}
