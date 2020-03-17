package uk.gov.ons.census.fwmt.jobservice.spg;

import uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

public final class SpgRequestBuilder {

  public static FieldworkFollowup makeUnitDeliver() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("U");
    fieldworkFollowup.setHandDeliver(true);

    return fieldworkFollowup;
  }

  public static FieldworkFollowup makeUnitFollowup() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("U");
    fieldworkFollowup.setHandDeliver(false);

    return fieldworkFollowup;
  }

  public static FieldworkFollowup makeSite() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("E");
    fieldworkFollowup.setSecureEstablishment(false);

    return fieldworkFollowup;
  }

  public static FieldworkFollowup makeSecureSite() {
    FieldworkFollowup fieldworkFollowup = makeBase();

    fieldworkFollowup.setAddressLevel("E");
    fieldworkFollowup.setSecureEstablishment(true);

    return fieldworkFollowup;
  }

  public static FieldworkFollowup makeBase() {
    return FieldworkFollowup.builder()
        .actionInstruction("CREATE")
        // TODO: Are you sure this can be re-enabled?
        .surveyName("CENSUS") // Not needed, but still in formal diagrams
        .addressType("SPG")

        .caseId("exampleCaseId")
        .caseRef("exampleCaseRef")
        .estabType("exampleEstabType")
        .fieldOfficerId("exampleOfficerId")
        .fieldCoordinatorId("exampleCoordinatorId")

        .organisationName("exampleOrgName")

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
