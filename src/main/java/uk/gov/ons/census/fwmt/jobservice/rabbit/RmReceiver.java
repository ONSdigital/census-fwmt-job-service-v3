package uk.gov.ons.census.fwmt.jobservice.rabbit;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

@Slf4j
@Component
@RabbitListener(queues = "${rabbitmq.queues.rm.input}", containerFactory = "retryContainerFactory")
public class RmReceiver {

  public static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  public static final String RM_CREATE_SWITCH_REQUEST_RECEIVED = "RM_CREATE_SWITCH_REQUEST_RECEIVED";
  public static final String RM_UPDATE_REQUEST_RECEIVED = "RM_UPDATE_REQUEST_RECEIVED";
  public static final String RM_CANCEL_REQUEST_RECEIVED = "RM_CANCEL_REQUEST_RECEIVED";
  public static final String RM_PAUSE_REQUEST_RECEIVED = "RM_PAUSE_REQUEST_RECEIVED";
  private static final String FAILED_TO_ROUTE_REQUEST = "FAILED_TO_ROUTE_REQUEST";
  @Autowired
  private final JobService jobService;
  @Autowired
  private final GatewayEventManager gatewayEventManager;

  public RmReceiver(JobService jobService, GatewayEventManager gatewayEventManager) {
    this.jobService = jobService;
    this.gatewayEventManager = gatewayEventManager;
  }

  @RabbitHandler
  public void receiveCreateMessage(FwmtActionInstruction rmRequest, Message message) throws GatewayException {
    //TODO trigger correct event CREATE or UPDATE
    Instant receivedMessageTime =message.getMessageProperties().getTimestamp().toInstant();
    System.out.println(receivedMessageTime);
    switch (rmRequest.getActionInstruction()) {
    case CREATE: {
      gatewayEventManager
          .triggerEvent(rmRequest.getCaseId(), RM_CREATE_REQUEST_RECEIVED,
              "Case Ref", rmRequest.getCaseRef());
      jobService.processCreate(rmRequest, receivedMessageTime);
      break;
    }
    case SWITCH_CE_TYPE: {
      gatewayEventManager
          .triggerEvent(rmRequest.getCaseId(), RM_CREATE_SWITCH_REQUEST_RECEIVED,
              "Case Ref", rmRequest.getCaseRef());
      jobService.processCreate(rmRequest, receivedMessageTime);
      break;
    }
    case UPDATE : {
      gatewayEventManager
          .triggerEvent(rmRequest.getCaseId(), RM_UPDATE_REQUEST_RECEIVED,
              "Case Ref", rmRequest.getCaseRef());
      jobService.processUpdate(rmRequest, receivedMessageTime);
      break;
    }
    default:
      break; //TODO THROW ROUTUNG FAILURE
    }
  }

  @RabbitHandler
  public void receiveCancelMessage(FwmtCancelActionInstruction rmRequest, Message message) throws GatewayException {
      //TODO trigger correct event CANCEL
    //TODO THROW ROUTUNG FAILURE
    Instant receivedMessageTime = message.getMessageProperties().getTimestamp().toInstant();
    if (rmRequest.getActionInstruction() == ActionInstructionType.CANCEL) {
      gatewayEventManager
          .triggerEvent(rmRequest.getCaseId(), RM_CANCEL_REQUEST_RECEIVED,
              "Case Ref", "N/A");
      jobService.processCancel(rmRequest, receivedMessageTime);
    } else {
      gatewayEventManager
          .triggerErrorEvent(this.getClass(), "Could not route Request", rmRequest.getCaseId(), FAILED_TO_ROUTE_REQUEST,
              "Action Request", rmRequest.getActionInstruction().toString());
      throw new RuntimeException("Could not route Request");
    }
  }
  
  public static void main(String[] args) {
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
        .withZone(ZoneId.systemDefault());

System.out.println(DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(1600169507861L)));
  }
  
}
