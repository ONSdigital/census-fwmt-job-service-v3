package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.time.Instant;

@RequiredArgsConstructor
@Slf4j
@Component
public class GWMessageProcessor {
  public static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  public static final String RM_CREATE_SWITCH_REQUEST_RECEIVED = "RM_CREATE_SWITCH_REQUEST_RECEIVED";
  public static final String RM_UPDATE_REQUEST_RECEIVED = "RM_UPDATE_REQUEST_RECEIVED";
  public static final String RM_CANCEL_REQUEST_RECEIVED = "RM_CANCEL_REQUEST_RECEIVED";
  public static final String FAILED_TO_ROUTE_REQUEST = "FAILED_TO_ROUTE_REQUEST";
  public static final String RM_PAUSE_REQUEST_RECEIVED = "RM_PAUSE_REQUEST_RECEIVED";

  private final JobService jobService;
  private final GatewayEventManager gatewayEventManager;
  private final TransientExceptionHandler transientExceptionHandler;

  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate gatewayRabbitTemplate;

  public void processCreateInstruction(FwmtActionInstruction instruction, Instant messageTime, Message message) {
    try {
      switch (instruction.getActionInstruction()) {
      case CREATE: {
        gatewayEventManager
            .triggerEvent(instruction.getCaseId(), RM_CREATE_REQUEST_RECEIVED,
                "Case Ref", instruction.getCaseRef());
        jobService.processCreate(instruction, messageTime);
        break;
      }
      case SWITCH_CE_TYPE: {
        gatewayEventManager
            .triggerEvent(instruction.getCaseId(), RM_CREATE_SWITCH_REQUEST_RECEIVED,
                "Case Ref", instruction.getCaseRef());
        jobService.processCreate(instruction, messageTime);
        break;
      }
      case UPDATE: {
        gatewayEventManager
            .triggerEvent(instruction.getCaseId(), RM_UPDATE_REQUEST_RECEIVED,
                "Case Ref", instruction.getCaseRef());
        jobService.processUpdate(instruction, messageTime);
        break;
      }
      case PAUSE: {
        gatewayEventManager.triggerEvent(instruction.getCaseId(), RM_PAUSE_REQUEST_RECEIVED,
            "Case Ref", instruction.getCaseRef());
        jobService.processPause(instruction, messageTime);
        break;
      }
      default:
        log.error("ActionInstruction not supported {} ", instruction.getActionInstruction());
      }
    } catch (RestClientException e) {
      log.error("- Create Message - Error sending message - {}  error - {} ", instruction, e.getMessage(), e);
      transientExceptionHandler.handleMessage(message);
    } catch (Exception e) {
      log.error("- Create Message - Error sending message - {}  error - {} ", instruction, e.getMessage(), e);
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.permanent.error", message);
    }
  }

  public void processCancelInstruction(FwmtCancelActionInstruction instruction, Instant messageTime, Message message) {

    try {

      if (instruction.getActionInstruction() == ActionInstructionType.CANCEL) {
        gatewayEventManager
            .triggerEvent(instruction.getCaseId(), RM_CANCEL_REQUEST_RECEIVED,
                "Case Ref", "N/A");
        jobService.processCancel(instruction, messageTime);
      } else {
        gatewayEventManager
            .triggerErrorEvent(this.getClass(), "Could not route Request", instruction.getCaseId(), FAILED_TO_ROUTE_REQUEST,
                "Action Request", instruction.getActionInstruction().toString());
        throw new RuntimeException("Could not route Request");
      }
    } catch (RestClientException e) {
      log.error("- Cancel Message - Error sending message - {}  error - {} ", instruction, e.getMessage(), e);
      transientExceptionHandler.handleMessage(message);
    } catch (Exception e) {
      log.error("- Cancel Message - Error sending message - {}  error - {} ", instruction, e.getMessage(), e);
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.permanent.error", message);
    }
  }

}
