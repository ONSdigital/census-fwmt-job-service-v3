package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;

import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

@Slf4j
@Service
public class JobService {

  private static final List<HttpStatus> validResponses = List
      .of(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.ACCEPTED);

  @Autowired
  private ConverterService converterService;
  @Autowired
  private CometRestClient cometRestClient;
  @Autowired
  private GatewayEventManager gatewayEventManager;
  @Autowired
  private GatewayCacheService gatewayCacheService;

  public void createFieldworkerJob(FieldworkFollowup ffu) throws GatewayException {
    CaseRequest putCase = converterService.buildPutCaseRequest(ffu);
    gatewayEventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CREATE_SENT, "Case Ref", ffu.getCaseRef());
    ResponseEntity<Void> response = cometRestClient.sendRequest(putCase, ffu.getCaseId());
    validateResponse(response, ffu.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);
    GatewayCache cache = gatewayCacheService.getById(ffu.getCaseId());
    // update the cache with existsInFwmt=true
    if (cache == null) {
      gatewayCacheService.save(GatewayCache.builder().caseId(ffu.getCaseId()).existsInFwmt(true).build());
    } else {
      gatewayCacheService.save(cache.toBuilder().existsInFwmt(true).build());
    }
    gatewayEventManager
        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CREATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
            response.getStatusCode().name());
  }

  private void validateResponse(ResponseEntity<Void> response, String caseId, String verb, String errorCode)
      throws GatewayException {
    if (!isValidResponse(response)) {
      String msg =
          "Unable to " + verb + " FieldWorkerJobRequest: HTTP_STATUS:" + response.getStatusCode() + ":" + response
              .getStatusCodeValue();
      gatewayEventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(caseId), errorCode);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }

  private boolean isValidResponse(ResponseEntity<Void> response) {
    return validResponses.contains(response.getStatusCode());
  }
}
