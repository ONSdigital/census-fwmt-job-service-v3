package uk.gov.ons.census.fwmt.jobservice.service;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK ;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.comet.CometRestClient;

@Slf4j
@Service
public class JobService {

  @Autowired
  private ConverterService converterService;

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager gatewayEventManager;

  private static final List<HttpStatus> validResponses = List.of(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.ACCEPTED);


  public void createFieldworkerJob(FieldworkFollowup ffu) throws GatewayException {
    CaseRequest putCase = converterService.buildPutCaseRequest(ffu);
    gatewayEventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CREATE_SENT, "Case Ref", ffu.getCaseRef());
    ResponseEntity<Void> response = cometRestClient.sendRequest(putCase, ffu.getCaseId());
    validateResponse(response, ffu.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);
    gatewayEventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_CREATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code", response.getStatusCode().name());
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
