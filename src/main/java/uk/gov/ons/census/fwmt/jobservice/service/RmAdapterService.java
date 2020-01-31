package uk.gov.ons.census.fwmt.jobservice.service;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

public interface RmAdapterService {

  void sendJobRequest(ActionInstruction actionInstruction) throws GatewayException;

}
