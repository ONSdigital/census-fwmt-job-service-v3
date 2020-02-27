package uk.gov.ons.census.fwmt.jobservice.comet.converter.spg;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.spg.SpgRequestBuilder;

class SpgSiteConverterTest {

  @Test
  void confirm_valid_spgRequest_can_be_converted() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    assertTrue(c.isValid(ffu));
  }

  @Test
  void confirm_spgRequest_with_nulls_returns_false() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction(null);
    ffu.setSurveyName(null);
    ffu.setAddressType(null);
    ffu.setAddressLevel(null);
    ffu.setSecureEstablishment(null);
    assertFalse(c.isValid(ffu));
  }

  @Test
  void confirm_spgRequest_with_invalid_actionInstrunction_returns_false() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction("nonsense");
    assertFalse(c.isValid(ffu));
  }

  @Test
  void confirm_spgRequest_with_invalid_surveyName_returns_false() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setSurveyName("nonsense");
    assertFalse(c.isValid(ffu));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressType_returns_false() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressType("nonsense");
    assertFalse(c.isValid(ffu));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressLevel_returns_false() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressLevel("nonsense");
    assertFalse(c.isValid(ffu));
  }

  @Test
  void confirm_spgRequest_with_invalid_secureEstablishment_returns_false() {
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setSecureEstablishment(true);
    assertFalse(c.isValid(ffu));
  }

  @Test
  void confirm_valid_spgRequest_creates_valid_TM_request() throws GatewayException{
    SpgSiteConverter c = new SpgSiteConverter();
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    CaseRequest actualTMRequest = c.convert(ffu);

    Contact contact = Contact.builder().
        name("exampleForename exampleSurname").
        organisationName("exampleOrgName").
        phone("examplePhoneNumber").
        email(null).build();

    String[] lines = {"exampleAddr1", "exampleAddr2", "exampleAddr3"};
    Address address = Address.builder().
        uprn(1l).
        lines(Arrays.asList(lines)).
        town("exampleTown").
        postcode("examplePostcode").build();

    Location location = Location.builder().
        lat(2.0f).
        _long(3.0f).build();

    CaseRequest expectedTMRequest = CaseRequest.builder().
        reference("exampleCaseRef").
        type(TypeEnum.CE).
        surveyType("SPG SITE").
        category("Not applicable").
        estabType("exampleEstabType").
        requiredOfficer("exampleOfficerId").
        coordCode("exampleCoordinatorId").
        contact(contact).
        address(address).
        location(location).
        build();

    assertEquals(expectedTMRequest, actualTMRequest);
  }
}
