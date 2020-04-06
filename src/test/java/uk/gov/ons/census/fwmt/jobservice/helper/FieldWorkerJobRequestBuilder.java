package uk.gov.ons.census.fwmt.jobservice.helper;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

import uk.gov.ons.census.fwmt.canonical.v1.Address;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.Contact;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;

public class FieldWorkerJobRequestBuilder {

  private static UUID EXAMPLE_CASE_ID = UUID.fromString("a48bf28e-e7f4-4467-a9fb-e000b6a55676");

  private Address createNewAddress() {
    Address address = new Address();

    address.setLine1("1 Station Road");
    address.setTownName("Town");
    address.setPostCode("AB1 2CD");
    address.setLatitude(BigDecimal.valueOf(1234.56));
    address.setLongitude(BigDecimal.valueOf(2345.67));
    address.setOa("oaTest");

    return address;
  }

  private Address createCCSNewAddress() {
    Address address = new Address();

    address.setPostCode("AB1 2CD");
    address.setLatitude(BigDecimal.valueOf(1234.56));
    address.setLongitude(BigDecimal.valueOf(2345.67));

    return address;
  }

  private CreateFieldWorkerJobRequest createDefaultCreateRequest() {
    CreateFieldWorkerJobRequest request = new CreateFieldWorkerJobRequest();

    request.setActionType("Create");
    request.setCaseId(EXAMPLE_CASE_ID);
    request.setCaseReference("caseReference");
    request.setCaseType("Household");
    request.setEstablishmentType("estabType");
    request.setCoordinatorId("coordId");

    request.setAddress(createNewAddress());

    Contact contact = new Contact();

    contact.setOrganisationName("Test");
    contact.setEmailAddress("Test@test.co.uk");
    contact.setPhoneNumber("07123456789");
    request.setContact(contact);

    request.setMandatoryResource("TestMand");

    return request;
  }

  public CreateFieldWorkerJobRequest createFieldWorkerJobRequestForConvert() {
    return createDefaultCreateRequest();
  }

  public CreateFieldWorkerJobRequest createFieldWorkerJobRequestForConvertWithoutContact() {
    CreateFieldWorkerJobRequest request = createDefaultCreateRequest();
    request.setContact(null);
    return request;
  }

  public CreateFieldWorkerJobRequest createFieldWorkerCEJobRequestForConvert() {
    CreateFieldWorkerJobRequest request = createDefaultCreateRequest();
    request.setCaseType("CE");
    request.setContact(null);
    return request;
  }

  public CreateFieldWorkerJobRequest createFieldWorkerCCSIVJobRequestForConvert() {
    CreateFieldWorkerJobRequest request = createDefaultCreateRequest();
    // TODO this used to be set, then later overridden by the default. Commented for posterity.
    // request.setMandatoryResource("ccsInterviewer");
    request.setCaseType("CCS");
    request.setEstablishmentType("Household");
    request.setAddress(createCCSNewAddress());
    request.setContact(null);
    return request;
  }

  public CreateFieldWorkerJobRequest createFieldWorkerCCSIVCeJobRequestForConvert() {
    CreateFieldWorkerJobRequest request = createDefaultCreateRequest();
    // TODO this used to be set, then later overridden by the default. Commented for posterity.
    // request.setMandatoryResource("ccsInterviewer");
    request.setCaseType("CCS");
    request.setEstablishmentType("CE");
    request.setAddress(createCCSNewAddress());
    request.setContact(null);
    return request;
  }

  public CreateFieldWorkerJobRequest createFieldWorkerCCSPLJobRequestForConvert() {
    CreateFieldWorkerJobRequest request = createDefaultCreateRequest();
    request.setCaseReference("123456789");
    // TODO this used to be set, then later overridden by the default. Commented for posterity.
    // request.setMandatoryResource("ccsInterviewer");
    request.setCaseType(null);
    request.setEstablishmentType(null);
    request.setAddress(createCCSNewAddress());
    request.setContact(null);
    return request;
  }

  public CancelFieldWorkerJobRequest cancelFieldWorkerJobRequest() {
    CancelFieldWorkerJobRequest cancelFieldWorkerJobRequest = new CancelFieldWorkerJobRequest();

    cancelFieldWorkerJobRequest.setCaseId(EXAMPLE_CASE_ID);
    cancelFieldWorkerJobRequest.setReason("TestReason");
    cancelFieldWorkerJobRequest.setActionType("actionType");
    cancelFieldWorkerJobRequest.setUntil(OffsetDateTime.parse("2030-01-01T00:00+00:00"));

    return cancelFieldWorkerJobRequest;
  }

  public UpdateFieldWorkerJobRequest updateFieldWorkerJobRequestWithPause() {
    UpdateFieldWorkerJobRequest updateFieldWorkerJobRequest = new UpdateFieldWorkerJobRequest();

    updateFieldWorkerJobRequest.setCaseId(EXAMPLE_CASE_ID);
    updateFieldWorkerJobRequest.setAddressType("HH");
    updateFieldWorkerJobRequest.setActionType("update");
    updateFieldWorkerJobRequest.setUaa(false);
    updateFieldWorkerJobRequest.setHoldUntil(OffsetDateTime.parse("2019-07-27T00:00+00:00"));
    updateFieldWorkerJobRequest.setBlankFormReturned(false);

    return updateFieldWorkerJobRequest;
  }

  public UpdateFieldWorkerJobRequest updateFieldWorkerJobRequestReinstate() {
    UpdateFieldWorkerJobRequest updateFieldWorkerJobRequest = new UpdateFieldWorkerJobRequest();

    updateFieldWorkerJobRequest.setCaseId(EXAMPLE_CASE_ID);
    updateFieldWorkerJobRequest.setAddressType("HH");
    updateFieldWorkerJobRequest.setActionType("update");
    updateFieldWorkerJobRequest.setUaa(false);
    updateFieldWorkerJobRequest.setHoldUntil(OffsetDateTime.parse("2019-05-26T00:00+00:00"));
    updateFieldWorkerJobRequest.setBlankFormReturned(true);

    return updateFieldWorkerJobRequest;
  }

  public CreateFieldWorkerJobRequest createFieldWorkerJobRequestForCCSIVConvert() {
    CreateFieldWorkerJobRequest createFieldWorkerJobRequest = createDefaultCreateRequest();

    createFieldWorkerJobRequest.setCaseReference("Test");
    createFieldWorkerJobRequest.setSurveyType("CCSIV");
    createFieldWorkerJobRequest.setEstablishmentType("Household");
    createFieldWorkerJobRequest.setMandatoryResource("Test_123");
    createFieldWorkerJobRequest.setCoordinatorId("123_abc");

    // TODO this used to be set, then later overridden by the default. Commented for posterity.
    // Address address = new Address();
    // address.setOa("!23_abc");
    // address.setLine1("1");
    // address.setLine2("Station Road");
    // address.setTownName("Chingford");
    // address.setPostCode("E4 7NG");
    // address.setLatitude(BigDecimal.valueOf(51));
    // address.setLongitude(BigDecimal.valueOf(0.0001));
    // createFieldWorkerJobRequest.setAddress(address);

    createFieldWorkerJobRequest.setCcsQuestionnaireURL("1133244");
    createFieldWorkerJobRequest.setSpecialInstructions("Test");

    return createFieldWorkerJobRequest;
  }

}
