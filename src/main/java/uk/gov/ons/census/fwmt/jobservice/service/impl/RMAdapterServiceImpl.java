package uk.gov.ons.census.fwmt.jobservice.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.canonical.CanonicalJobHelper;
import uk.gov.ons.census.fwmt.jobservice.message.GatewayActionProducer;
import uk.gov.ons.census.fwmt.jobservice.redis.HouseholdStore;
import uk.gov.ons.census.fwmt.jobservice.service.RMAdapterService;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANONICAL_CANCEL_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANONICAL_CREATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CANONICAL_UPDATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.INVALID_ACTION_INSTRUCTION;

@Slf4j
@Component
public class RMAdapterServiceImpl implements RMAdapterService {

  private final GatewayEventManager gatewayEventManager;
  private final GatewayActionProducer gatewayActionProducer;
  private final HouseholdStore householdStore;
  private final CanonicalJobHelper canonicalJobHelper;

  public RMAdapterServiceImpl(
      GatewayEventManager gatewayEventManager,
      GatewayActionProducer gatewayActionProducer,
      HouseholdStore householdStore
      // CanonicalJobHelper canonicalJobHelper
  ) {
    this.gatewayEventManager = gatewayEventManager;
    this.gatewayActionProducer = gatewayActionProducer;
    this.householdStore = householdStore;
    this.canonicalJobHelper = new CanonicalJobHelper();
  }

  public void sendJobRequest(ActionInstruction actionInstruction) throws GatewayException {
    if (actionInstruction.getActionRequest() != null) {
      sendCreateMessage(actionInstruction);
    } else if (actionInstruction.getActionCancel() != null) {
      createCancelMessage(actionInstruction);
    } else if (actionInstruction.getActionUpdate() != null) {
      createUpdateMessage(actionInstruction);
    } else {
      String msg = "No matching request was found";
      String unknown = "Unknown caseId";
      gatewayEventManager.triggerErrorEvent(this.getClass(), msg, unknown, INVALID_ACTION_INSTRUCTION);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, msg);
    }
  }

  private void createUpdateMessage(ActionInstruction actionInstruction) throws GatewayException {
    if (householdStore.retrieveCache(actionInstruction.getActionUpdate().getCaseId()) == null)
      return;

    if (actionInstruction.getActionUpdate().getAddressType().equals("HH")) {
      gatewayActionProducer.sendMessage(canonicalJobHelper.newUpdateJob(actionInstruction));
      gatewayEventManager.triggerEvent(actionInstruction.getActionUpdate().getCaseId(), CANONICAL_UPDATE_SENT);
    } else {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Valid address type not found");
    }
  }

  private void createCancelMessage(ActionInstruction actionInstruction) throws GatewayException {
    if (householdStore.retrieveCache(actionInstruction.getActionCancel().getCaseId()) == null)
      return;

    if (householdStore.retrieveCache(actionInstruction.getActionCancel().getCaseId()) != null) {
      if (actionInstruction.getActionCancel().getAddressType().equals("HH")) {

        gatewayActionProducer.sendMessage(canonicalJobHelper.newCancelJob(actionInstruction));
        gatewayEventManager
            .triggerEvent(actionInstruction.getActionCancel().getCaseId(), CANONICAL_CANCEL_SENT, "Case Ref",
                actionInstruction.getActionCancel().getCaseRef());
      } else {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Valid address type not found");
      }
    }
  }

  private void sendCreateMessage(ActionInstruction actionInstruction) throws GatewayException {
    householdStore.cacheJob(actionInstruction.getActionRequest().getCaseId());
    gatewayActionProducer.sendMessage(canonicalJobHelper.newCreateJob(actionInstruction));
    gatewayEventManager.triggerEvent(actionInstruction.getActionRequest().getCaseId(), CANONICAL_CREATE_SENT);
  }
}
