package uk.gov.ons.census.fwmt.jobservice.spg;

import uk.gov.ons.census.fwmt.jobservice.dto.tm.Address;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.CaseType;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.Contact;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.Location;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.PutCaseRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

public class SpgPutCaseBuilder {

  public PutCaseRequest makeUnitDeliver() {
    return makeBase("SPG Unit-D");
  }

  public PutCaseRequest makeUnitFollowup() {
    return makeBase("SPG Unit-F");
  }

  public PutCaseRequest makeSite() {
    return makeBase("SPG SITE");
  }

  public PutCaseRequest makeSecureSite() {
    return makeBase("SPG SECURE SITE");
  }

  public PutCaseRequest makeBase(String surveyType) {
    return PutCaseRequest.builder()
        .reference("exampleCaseRef")
        .type(CaseType.CE)
        .surveyType(surveyType)
        .category("Not applicable")
        .estabType("exampleEstabType")
        .requiredOfficer("exampleOfficerId")
        .coordCode("exampleCoordinatorId")

        .contact(Contact.builder()
            .organisationName("exampleOrgName")
            .name("exampleForename exampleSurname")
            .phone("examplePhoneNumber")
            .build())

        .address(Address.builder()
            .uprn(1L)
            .lines(List.of("exampleAddr1", "exampleAddr2", "exampleAddr3"))
            .town("exampleTown")
            .postcode("examplePostcode")
            .build())

        .location(Location.builder()
            .lat(2F)
            ._long(3F)
            .build())

        .uaa(false)
        .sai(false)

        .build();
  }
}
