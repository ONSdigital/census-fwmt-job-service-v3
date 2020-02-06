package uk.gov.ons.census.fwmt.jobservice.consumer.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.jobservice.service.JobServiceOld;

import java.io.IOException;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANONICAL_CANCEL_RECEIVED;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANONICAL_CREATE_JOB_RECEIVED;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANONICAL_UPDATE_RECEIVED;

@Slf4j
//@Component
public class GatewayActionsReceiver {

  private final JobServiceOld jobServiceOld;
  private final GatewayEventManager gatewayEventManager;
  private final ObjectMapper jsonObjectMapper;
  private final MessageConverter messageConverter;

  public GatewayActionsReceiver(
      JobServiceOld jobServiceOld,
      GatewayEventManager gatewayEventManager,
      //ObjectMapper jsonObjectMapper,
      MessageConverter messageConverter) {
    this.jobServiceOld = jobServiceOld;
    this.gatewayEventManager = gatewayEventManager;
    this.jsonObjectMapper = new ObjectMapper();
    this.messageConverter = messageConverter;
  }

  public void receiveMessage(Object message) throws GatewayException {
    log.info("received a message from RM-Adapter");
    convertAndSendMessage(message);
  }

  private void convertAndSendMessage(Object actualMessage) throws GatewayException {
    String messageToString = actualMessage.toString();
    JsonNode actualMessageRootNode;
    try {
      actualMessageRootNode = jsonObjectMapper.readTree(messageToString);
    } catch (IOException e) {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Cannot process message JSON");
    }
    JsonNode gatewayType = actualMessageRootNode.path("gatewayType");
    JsonNode caseId = actualMessageRootNode.path("caseId");

    switch (gatewayType.asText()) {
    case "Create":
      CreateFieldWorkerJobRequest fwmtCreateJobRequest = messageConverter
          .convertMessageToDTO(CreateFieldWorkerJobRequest.class,
              messageToString);
      gatewayEventManager.triggerEvent(String.valueOf(fwmtCreateJobRequest.getCaseId()), CANONICAL_CREATE_JOB_RECEIVED);
      jobServiceOld.createJob(fwmtCreateJobRequest);
      break;
    case "Cancel":
      CancelFieldWorkerJobRequest fwmtCancelJobRequest = messageConverter
          .convertMessageToDTO(CancelFieldWorkerJobRequest.class,
              messageToString);
      gatewayEventManager.triggerEvent(String.valueOf(fwmtCancelJobRequest.getCaseId()), CANONICAL_CANCEL_RECEIVED);
      jobServiceOld.cancelJob(fwmtCancelJobRequest);
      break;
    case "Update":
      UpdateFieldWorkerJobRequest fwmtUpdateJobRequest = messageConverter
          .convertMessageToDTO(UpdateFieldWorkerJobRequest.class,
              messageToString);
      gatewayEventManager.triggerEvent(String.valueOf(fwmtUpdateJobRequest.getCaseId()), CANONICAL_UPDATE_RECEIVED);
      jobServiceOld.updateJob(fwmtUpdateJobRequest);
      break;
    default:
      String errorMsg = "Invalid Canonical Action.";
      gatewayEventManager.triggerErrorEvent(this.getClass(), errorMsg, "<UNKNOWN_CASE_ID>",
          GatewayEventsConfig.INVALID_CANONICAL_ACTION);
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, errorMsg);
    }
    log.info("Sending " + caseId.asText() + " job to TM");
  }
}