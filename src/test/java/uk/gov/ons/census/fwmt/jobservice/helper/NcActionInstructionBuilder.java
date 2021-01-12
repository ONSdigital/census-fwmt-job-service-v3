package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

import java.util.UUID;

public class NcActionInstructionBuilder {

  public FwmtActionInstruction createNcActionInstruction() {
    FwmtActionInstruction actionInstruction = new FwmtActionInstruction();
    String oldCaseId = "ac623e62-4f4b-11eb-ae93-0242ac130002";
    actionInstruction.setActionInstruction(ActionInstructionType.CREATE);
    actionInstruction.setSurveyName("CENSUS");
    actionInstruction.setAddressType("HH");
    actionInstruction.setAddressLevel("U");
    actionInstruction.setOa("E00167164");
    actionInstruction.setCaseId(String.valueOf(UUID.randomUUID()));
    actionInstruction.setOldCaseId(oldCaseId);
    actionInstruction.setCaseRef("NC7541877481");
    actionInstruction.setNc(true);
    actionInstruction.setAddressLine1("10 Test Street");
    actionInstruction.setTownName("Test Town");
    actionInstruction.setPostcode("TT TS1");
    actionInstruction.setEstabType("Hotel");
    actionInstruction.setFieldCoordinatorId("Test123");
    actionInstruction.setLatitude(50.0000);
    actionInstruction.setLongitude(1.0000);
    actionInstruction.setBlankFormReturned(true);
    actionInstruction.setUndeliveredAsAddress(true);
    return actionInstruction;
  }
}
