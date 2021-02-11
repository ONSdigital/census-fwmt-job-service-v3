package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

public class FwmtCancelJobRequestBuilder {

  public FwmtCancelActionInstruction cancelActionInstruction() {
    FwmtCancelActionInstruction fwmtCancelActionInstruction = new FwmtCancelActionInstruction();
    fwmtCancelActionInstruction.setActionInstruction(ActionInstructionType.CANCEL);
    fwmtCancelActionInstruction.setNc(false);
    fwmtCancelActionInstruction.setCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002");
    fwmtCancelActionInstruction.setAddressLevel("E");
    fwmtCancelActionInstruction.setAddressType("CE");
    return fwmtCancelActionInstruction;
  }
}
