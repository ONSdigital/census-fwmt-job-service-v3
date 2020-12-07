package uk.gov.ons.census.fwmt.jobservice.helper;

import uk.gov.ons.census.fwmt.common.data.ccs.CCSPropertyListingOutcome;
import uk.gov.ons.census.fwmt.common.data.shared.Address;
import uk.gov.ons.census.fwmt.common.data.shared.CareCode;
import uk.gov.ons.census.fwmt.common.data.shared.CeDetails;

import java.util.ArrayList;
import java.util.List;

public class CcsPropertyListedOutcomeBuilder {

  public CCSPropertyListingOutcome createCcsPropertyListingCeOutcome() {
    CCSPropertyListingOutcome ccsPropertyListingOutcome = new CCSPropertyListingOutcome();

    CeDetails ceDetails = new CeDetails();

    ceDetails.setUsualResidents(10);
    ceDetails.setBedspaces("15");
    ceDetails.setContactPhone("0123456789");
    ceDetails.setEstablishmentType("CE");
    ccsPropertyListingOutcome.setCeDetails(ceDetails);

    Address address = new Address();
    ccsPropertyListingOutcome.setAddress(address);

    ccsPropertyListingOutcome.setAccessInfo("Use gate");
    CareCode careCode = new CareCode();
    careCode.setCareCode("Mad dog");

    List<CareCode> careCodes = new ArrayList<>();
    careCodes.add(careCode);
    ccsPropertyListingOutcome.setCareCodes(careCodes);

    return ccsPropertyListingOutcome;
  }

  public CCSPropertyListingOutcome createCcsPropertyListingOutcome() {
    CCSPropertyListingOutcome ccsPropertyListingOutcome = new CCSPropertyListingOutcome();

    Address address = new Address();
    ccsPropertyListingOutcome.setAddress(address);

    ccsPropertyListingOutcome.setAccessInfo("Use gate");
    CareCode careCode = new CareCode();
    careCode.setCareCode("Mad dog");

    List<CareCode> careCodes = new ArrayList<>();
    careCodes.add(careCode);
    ccsPropertyListingOutcome.setCareCodes(careCodes);

    return ccsPropertyListingOutcome;
  }
}
