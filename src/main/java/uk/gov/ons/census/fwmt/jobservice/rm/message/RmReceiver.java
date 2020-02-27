package uk.gov.ons.census.fwmt.jobservice.rm.message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
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

  public void receiveMessage(FieldworkFollowup ffu) throws GatewayException, JsonProcessingException {
    gatewayEventManager.triggerEvent(ffu.getCaseId(), GatewayEventsConfig.RM_CREATE_REQUEST_RECEIVED, "Case Ref", ffu.getCaseRef());
    jobService.createFieldworkerJob(ffu);
  }

}
