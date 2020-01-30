package uk.gov.ons.census.fwmt.jobservice.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.rmadapter.config.GatewayEventsConfig;
import uk.gov.ons.census.fwmt.rmadapter.service.RMAdapterService;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;

@Component
@Slf4j
public class ActionInstructionReceiver {

  @Autowired
  private RMAdapterService rmAdapterService;

  @Autowired
  private GatewayEventManager gatewayEventManager;

  private JAXBContext jaxbContext;

  public ActionInstructionReceiver() throws JAXBException {
    jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
  }

  public void receiveMessage(String message) throws GatewayException {
    try {
      // This should be moved to Queue Config, but cant get it to work
      Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
      ByteArrayInputStream input = new ByteArrayInputStream(message.getBytes());
      JAXBElement<ActionInstruction> rmActionInstruction = unmarshaller
          .unmarshal(new StreamSource(input), ActionInstruction.class);
      // ===============================================================
      triggerEvent(rmActionInstruction.getValue());
      rmAdapterService.sendJobRequest(rmActionInstruction.getValue());
    } catch (JAXBException e) {
      String msg = "Failed to unmarshal XML message.";
      gatewayEventManager.triggerErrorEvent(this.getClass(), e, msg, "<UNKNOWN_CASE_ID>",
          GatewayEventsConfig.FAILED_TO_UNMARSHALL_ACTION_INSTRUCTION);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg, e);
    }
  }

  private void triggerEvent(ActionInstruction actionInstruction) {
    if (actionInstruction.getActionRequest() != null) {
      gatewayEventManager.triggerEvent(actionInstruction.getActionRequest().getCaseId(),
          GatewayEventsConfig.RM_CREATE_REQUEST_RECEIVED, "Case Ref",
          actionInstruction.getActionRequest().getCaseRef());
    } else if (actionInstruction.getActionCancel() != null) {
      gatewayEventManager
          .triggerEvent(actionInstruction.getActionCancel().getCaseId(), GatewayEventsConfig.RM_CANCEL_REQUEST_RECEIVED,
              "Case Ref", actionInstruction.getActionCancel().getCaseRef());
    } else if (actionInstruction.getActionUpdate() != null) {
      gatewayEventManager.triggerEvent(actionInstruction.getActionUpdate().getCaseId(),
          GatewayEventsConfig.RM_UPDATE_REQUEST_RECEIVED);
    }
  }
}
