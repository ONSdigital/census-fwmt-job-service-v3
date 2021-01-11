package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RabbitListener(queues = "${app.rabbitmq.rm.queues.rm.input}", containerFactory = "rmContainerFactory", concurrency = "${app.rabbitmq.rm.concurrentConsumers}")
public class RmReceiver {

  public static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  public static final String RM_CREATE_SWITCH_REQUEST_RECEIVED = "RM_CREATE_SWITCH_REQUEST_RECEIVED";
  public static final String RM_UPDATE_REQUEST_RECEIVED = "RM_UPDATE_REQUEST_RECEIVED";
  public static final String RM_CANCEL_REQUEST_RECEIVED = "RM_CANCEL_REQUEST_RECEIVED";
  private static final String FAILED_TO_ROUTE_REQUEST = "FAILED_TO_ROUTE_REQUEST";
  private static final String RM_PAUSE_REQUEST_RECEIVED = "RM_PAUSE_REQUEST_RECEIVED";

  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate gatewayRabbitTemplate;

  @Autowired
  private final JobService jobService;
  @Autowired
  private final GatewayEventManager gatewayEventManager;

  public RmReceiver(JobService jobService, GatewayEventManager gatewayEventManager) {
    this.jobService = jobService;
    this.gatewayEventManager = gatewayEventManager;
  }

  public static void main(String[] args) {
    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSZ")
        .withZone(ZoneId.systemDefault());

    System.out.println(DATE_TIME_FORMATTER.format(Instant.ofEpochMilli(1600169507861L)));
  }

  @RabbitHandler
  public void receiveCreateMessage(FwmtActionInstruction rmRequest, @Header("timestamp") String timestamp, Message message) throws GatewayException {
    //TODO trigger correct event CREATE or UPDATE
    try {
      long epochTimeStamp = Long.parseLong(timestamp);
      Instant receivedMessageTime = Instant.ofEpochMilli(epochTimeStamp);
      System.out.println(receivedMessageTime);
      System.out.println(message.getMessageProperties());
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
      case UPDATE: {
        gatewayEventManager
            .triggerEvent(rmRequest.getCaseId(), RM_UPDATE_REQUEST_RECEIVED,
                "Case Ref", rmRequest.getCaseRef());
        jobService.processUpdate(rmRequest, receivedMessageTime);
        break;
      }
      case PAUSE: {
        gatewayEventManager.triggerEvent(rmRequest.getCaseId(), RM_PAUSE_REQUEST_RECEIVED,
            "Case Ref", rmRequest.getCaseRef());
        jobService.processPause(rmRequest, receivedMessageTime);
        break;
      }
      default:
        break; //TODO THROW ROUTUNG FAILURE
      }
    } catch (Exception e) {
      log.error("- Create Message - Error sending message - {}  error - {} ", rmRequest, e.getMessage(), e);
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.receiver.error", message);
    }
  }

  @RabbitHandler
  public void receiveCancelMessage(FwmtCancelActionInstruction rmRequest, @Header("timestamp") String timestamp, Message message)  {
    //TODO trigger correct event CANCEL
    //TODO THROW ROUTUNG FAILURE
    try {
      long epochTimeStamp = Long.parseLong(timestamp);
      Instant receivedMessageTime = Instant.ofEpochMilli(epochTimeStamp);
      System.out.println(message.getMessageProperties());
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
    }catch(RestClientException e ){
      log.error("Cannot connect to TM -  Error sending message - {}  error - {} ", rmRequest, e.getMessage(), e);
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.transient.error", message);
    }
    catch (Exception e) {
      log.error("- Cancel Message - Error sending message - {}  error - {} ", rmRequest, e.getMessage(), e);
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.permanent.error", message);
    }
  }

}
