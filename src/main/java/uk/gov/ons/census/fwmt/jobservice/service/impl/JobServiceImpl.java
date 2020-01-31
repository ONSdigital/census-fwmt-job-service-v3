package uk.gov.ons.census.fwmt.jobservice.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.rest.client.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CANCEL_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_UPDATE_TM_JOB;

@Service
public class JobServiceImpl implements JobService {

  private static final List<HttpStatus> validResponses = List
      .of(HttpStatus.OK, HttpStatus.CREATED, HttpStatus.ACCEPTED);

  private final CometRestClient cometRestClient;
  private final Map<String, CometConverter> cometConverters;
  private final GatewayEventManager gatewayEventManager;

  public JobServiceImpl(
      CometRestClient cometRestClient,
      Map<String, CometConverter> cometConverters,
      GatewayEventManager gatewayEventManager) {
    this.cometRestClient = cometRestClient;
    this.cometConverters = cometConverters;
    this.gatewayEventManager = gatewayEventManager;
  }

  @Override
  public void createJob(CreateFieldWorkerJobRequest jobRequest) throws GatewayException {
    convertAndSendCreate(jobRequest);
  }

  @Override
  public void cancelJob(CancelFieldWorkerJobRequest cancelRequest) throws GatewayException {
    convertAndSendCancel(cancelRequest);
  }

  @Override
  public void updateJob(UpdateFieldWorkerJobRequest updateRequest) throws GatewayException {
    convertAndSendUpdate(updateRequest);
  }

  @Override
  public void convertAndSendCreate(CreateFieldWorkerJobRequest jobRequest) throws GatewayException {
    final CometConverter cometConverter = cometConverters.get(jobRequest.getCaseType());
    CaseRequest caseRequest = cometConverter.convert(jobRequest);
    gatewayEventManager.triggerEvent(String.valueOf(jobRequest.getCaseId()), COMET_CREATE_SENT, "Case Ref",
        jobRequest.getCaseReference());
    ResponseEntity<Void> response = cometRestClient.sendRequest(caseRequest, String.valueOf(jobRequest.getCaseId()));
    validateResponse(response, jobRequest.getCaseId(), "Create", FAILED_TO_CREATE_TM_JOB);
    gatewayEventManager.triggerEvent(String.valueOf(jobRequest.getCaseId()), COMET_CREATE_ACK, "Response Code",
        response.getStatusCode().name());
  }

  public void convertAndSendCancel(CancelFieldWorkerJobRequest cancelJobRequest) throws GatewayException {
    final CometConverter cometConverter = cometConverters.get("HH");
    CasePauseRequest casePauseRequest = cometConverter.convertCancel(cancelJobRequest);
    gatewayEventManager.triggerEvent(String.valueOf(cancelJobRequest.getCaseId()), COMET_CANCEL_SENT);
    ResponseEntity<Void> response = cometRestClient
        .sendRequest(casePauseRequest, String.valueOf(cancelJobRequest.getCaseId()));
    validateResponse(response, cancelJobRequest.getCaseId(), "Cancel", FAILED_TO_CANCEL_TM_JOB);
    gatewayEventManager.triggerEvent(String.valueOf(cancelJobRequest.getCaseId()), COMET_CANCEL_ACK, "Response Code",
        response.getStatusCode().name());
  }

  public void convertAndSendUpdate(UpdateFieldWorkerJobRequest updateRequest) throws GatewayException {
    final CometConverter cometConverter = cometConverters.get("HH");
    ModelCase modelCase = cometRestClient.getCase(String.valueOf(updateRequest.getCaseId()));
    CaseRequest caseRequest = cometConverter.convertUpdate(updateRequest, modelCase);
    gatewayEventManager.triggerEvent(String.valueOf(updateRequest.getCaseId()), COMET_UPDATE_SENT);
    if (!StringUtils.isEmpty(caseRequest.getPause())) {
      CasePauseRequest casePauseRequest = caseRequest.getPause();
      ResponseEntity<Void> response = cometRestClient
          .sendRequest(casePauseRequest, String.valueOf(updateRequest.getCaseId()));
      validateResponse(response, updateRequest.getCaseId(), "Pause", FAILED_TO_UPDATE_TM_JOB);
    }
    ResponseEntity<Void> response = cometRestClient.sendRequest(caseRequest, String.valueOf(updateRequest.getCaseId()));
    validateResponse(response, updateRequest.getCaseId(), "Pause", FAILED_TO_UPDATE_TM_JOB);
    gatewayEventManager.triggerEvent(String.valueOf(updateRequest.getCaseId()), COMET_UPDATE_ACK, "Response Code",
        response.getStatusCode().name());
  }

  private boolean isValidResponse(ResponseEntity<Void> response) {
    return validResponses.contains(response.getStatusCode());
  }

  private void validateResponse(ResponseEntity<Void> response, UUID caseId, String verb, String errorCode)
      throws GatewayException {
    if (!isValidResponse(response)) {
      String msg =
          "Unable to " + verb + " FieldWorkerJobRequest: HTTP_STATUS:" + response.getStatusCode() + ":" + response
              .getStatusCodeValue();
      gatewayEventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(caseId), errorCode);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }

}
