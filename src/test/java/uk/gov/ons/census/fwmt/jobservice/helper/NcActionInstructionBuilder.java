package uk.gov.ons.census.fwmt.jobservice.helper;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;

import java.util.UUID;

public class NcActionInstructionBuilder {
  @Autowired
  private GatewayCacheService gatewayCacheService;

  public FwmtActionInstruction createOriginalActionInstruction() {
    FwmtActionInstruction actionInstruction = new FwmtActionInstruction();
    actionInstruction.setActionInstruction(ActionInstructionType.CREATE);
    actionInstruction.setSurveyName("CENSUS");
    actionInstruction.setAddressType("HH");
    actionInstruction.setAddressLevel("U");
    actionInstruction.setOa("E00167164");
    actionInstruction.setCaseId(String.valueOf("ac623e62-4f4b-11eb-ae93-0242ac130002"));
    actionInstruction.setCaseRef("NC7541877481");
    actionInstruction.setNc(false);
    actionInstruction.setAddressLine1("10 Test Street");
    actionInstruction.setTownName("Test Town");
    actionInstruction.setPostcode("TT TS1");
    //    createCacheEntry();
    return actionInstruction;
  }

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
//    createCacheEntry();
    return actionInstruction;
  }

//  private void createCacheEntry() {
//    gatewayCacheService.save(GatewayCache.builder().caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").careCodes("Test Care").build());
//  }
}
