package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

@Slf4j
@Component
public class RmReceiver {

  @Autowired
  private JobService jobService;

  @Autowired
  private GatewayEventManager gatewayEventManager;

  public RmReceiver(JobService jobService, GatewayEventManager gatewayEventManager) {
    this.jobService = jobService;
    this.gatewayEventManager = gatewayEventManager;
  }

  public void receiveMessage(FwmtActionInstruction rmRequest) throws GatewayException {
    //TODO trigger correct event CREATE or UPDATE
    switch (rmRequest.getActionInstruction()) {
      case CREATE: {
        gatewayEventManager
            .triggerEvent(rmRequest.getCaseId(), GatewayEventsConfig.RM_CREATE_REQUEST_RECEIVED,
                "Case Ref", rmRequest.getCaseRef());
        jobService.processCreate(rmRequest); break;
      }
      case SWITCH_CE_TYPE: {
        gatewayEventManager
        .triggerEvent(rmRequest.getCaseId(), GatewayEventsConfig.CREATE_SWITCH_REQUEST_RECEIVED,
            "Case Ref", rmRequest.getCaseRef());
        jobService.processCreate(rmRequest); break;
      }
      case UPDATE : {
        gatewayEventManager
        .triggerEvent(rmRequest.getCaseId(), GatewayEventsConfig.RM_UPDATE_REQUEST_RECEIVED,
            "Case Ref", rmRequest.getCaseRef());
        jobService.processUpdate(rmRequest); break;
      }
        default : break; //TODO THROW ROUTUNG FAILURE
    }
  }

  public void receiveMessage(FwmtCancelActionInstruction rmRequest) throws GatewayException {
      //TODO trigger correct event CANCEL
    //TODO THROW ROUTUNG FAILURE
    if (rmRequest.getActionInstruction() == ActionInstructionType.CANCEL) {
      gatewayEventManager
          .triggerEvent(rmRequest.getCaseId(), GatewayEventsConfig.RM_CANCEL_REQUEST_RECEIVED,
              "Case Ref", "N/A");
      jobService.processCancel(rmRequest);
    }
      }
}
