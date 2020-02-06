package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.dto.rm.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.PutCaseRequest;
import uk.gov.ons.census.fwmt.jobservice.service.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.mapper.SpgMapper;

@Slf4j
@Service
public class JobService {

  private final SpgMapper spgMapper;
  private final CometRestClient cometRestClient;
  private final GatewayEventManager gatewayEventManager;

  public JobService(SpgMapper spgMapper, CometRestClient cometRestClient, GatewayEventManager gatewayEventManager) {
    this.spgMapper = spgMapper;
    this.cometRestClient = cometRestClient;
    this.gatewayEventManager = gatewayEventManager;
  }

  public void handleMessage(FieldworkFollowup fieldworkFollowup) throws GatewayException {
    PutCaseRequest putCase = map(fieldworkFollowup);

    cometRestClient.sendRequest(putCase, fieldworkFollowup.getCaseId());
  }

  public PutCaseRequest map(FieldworkFollowup fieldworkFollowup) {
    if (fieldworkFollowup.getAddressType().equals("SPG")) {
      return spgMapper.map(fieldworkFollowup);
    } else {
      // TODO proper error handling
      return null;
    }
  }

}
