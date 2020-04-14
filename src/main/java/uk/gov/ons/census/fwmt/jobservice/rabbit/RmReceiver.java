package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
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

  public void receiveMessage(FwmtActionInstruction ffu) throws GatewayException {
    gatewayEventManager
        .triggerEvent(ffu.getCaseId(), GatewayEventsConfig.RM_CREATE_REQUEST_RECEIVED, "Case Ref", ffu.getCaseRef());
    jobService.process(ffu);
  }

  public void receiveMessage(FwmtCancelActionInstruction ffu) throws GatewayException {
    gatewayEventManager
        .triggerEvent(ffu.getCaseId(), GatewayEventsConfig.RM_CREATE_REQUEST_RECEIVED, "Case Ref", "N/A");
    jobService.process(ffu);
  }
}
