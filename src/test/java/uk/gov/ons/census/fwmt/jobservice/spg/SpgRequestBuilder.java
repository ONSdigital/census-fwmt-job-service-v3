package uk.gov.ons.census.fwmt.jobservice.spg;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.jobservice.dto.rm.FieldworkFollowup;

@Component
public class SpgRequestBuilder {

  public FieldworkFollowup makeUnitDeliver() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("U");
    fieldworkFollowup.setHandDeliver(true);

    return fieldworkFollowup;
  }

  public FieldworkFollowup makeUnitFollowup() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("U");
    fieldworkFollowup.setHandDeliver(false);

    return fieldworkFollowup;
  }

  public FieldworkFollowup makeSite() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("E");
    fieldworkFollowup.setSecureEstablishment(false);

    return fieldworkFollowup;
  }

  public FieldworkFollowup makeSecureSite() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("E");
    fieldworkFollowup.setSecureEstablishment(true);

    return fieldworkFollowup;
  }

  public FieldworkFollowup makeBase() {
    return FieldworkFollowup.builder()
        .actionInstruction("CREATE")
        //.surveyName("Census"); // Not needed, but still in formal diagrams
        .addressType("SPG")

        .caseId("exampleCaseId")
        .caseRef("exampleCaseRef")
        .estabType("exampleEstabType")
        .fieldOfficerId("exampleOfficerId")
        .fieldCoordinatorId("exampleCoordinatorId")

        .organisationName("exampleOrgName")
        .forename("exampleForename")
        .surname("exampleSurname")
        .phoneNumber("examplePhoneNumber")

        .uprn("1")
        .addressLine1("exampleAddr1")
        .addressLine2("exampleAddr2")
        .addressLine3("exampleAddr3")
        .townName("exampleTown")
        .postcode("examplePostcode")
        .oa("exampleOa")

        .latitude("2")
        .longitude("3")
        .uaa(false)

        .build();
  }
}
