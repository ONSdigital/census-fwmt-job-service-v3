package uk.gov.ons.census.fwmt.jobservice.converter;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

@Component
@Slf4j
public class CometConverterUtils {

  @Autowired
  private List<CometConverter> selectors;

  public CaseRequest buildPutCaseRequest(FieldworkFollowup ffu) throws GatewayException {
    CometConverter selector = selectors.stream().filter(s -> s.isValid(ffu)).findFirst().get();
    CaseRequest caseRequest = selector.convert(ffu);
    return caseRequest;
  }
}
