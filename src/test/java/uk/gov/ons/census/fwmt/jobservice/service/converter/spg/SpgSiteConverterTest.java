package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.Type;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.spg.SpgRequestBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpgSiteConverterTest {
  private final SpgCreateCommon converter;

  public SpgSiteConverterTest() {
    SpgFollowUpSchedulingService spgFollowUpSchedulingService = Mockito.mock(SpgFollowUpSchedulingService.class);
    List<CometConverter> selectors = List.of(
        new SpgCreateUnitFollowupConverter(spgFollowUpSchedulingService),
        new SpgCreateUnitDeliverConverter(),
        new SpgCreateSiteConverter(),
        new SpgCreateSecureSiteConverter()
    );
    this.converter = new SpgCreateCommon(selectors);
  }

  @Test
  void confirm_valid_spgRequest_can_be_converted() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    GatewayCache cache = GatewayCache.builder().build();
    assertTrue(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_nulls_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction(null);
    ffu.setSurveyName(null);
    ffu.setAddressType(null);
    ffu.setAddressLevel(null);
    ffu.setSecureEstablishment(null);
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_actionInstrunction_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_surveyName_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setSurveyName("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressType_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressType("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressLevel_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressLevel("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_secureEstablishment_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSecureSite();
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(converter.isValid(ffu, cache));
  }

  @Test
  void confirm_valid_spgRequest_creates_valid_TM_request() throws GatewayException {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    GatewayCache cache = GatewayCache.builder()
        .managerTitle("Mx")
        .managerFirstname("exampleForename")
        .managerSurname("exampleSurname")
        .contactPhoneNumber("examplePhoneNumber")
        .build();

    CaseRequest actualTMRequest = converter.convert(ffu, cache, CaseRequest.builder()).build();

    Contact contact = Contact.builder()
        .name("Mx exampleForename exampleSurname")
        .organisationName("exampleOrgName")
        .phone("examplePhoneNumber")
        .email(null)
        .build();

    Address address = Address.builder()
        .lines(List.of("exampleAddr1", "exampleAddr2", "exampleAddr3"))
        .town("exampleTown")
        .postcode("examplePostcode").build();

    Location location = Location.builder().lat(2.0f)._long(3.0f).build();

    CaseRequest expectedTMRequest = CaseRequest.builder()
        .reference("exampleCaseRef")
        .type(Type.CE)
        .surveyType(CaseRequest.SurveyType.SPG_Site)
        .category("Not applicable")
        .estabType("exampleEstabType")
        .requiredOfficer("exampleOfficerId")
        .coordCode("exampleCoordinatorId")
        .contact(contact)
        .address(address)
        .location(location)
        .build();

    assertEquals(expectedTMRequest, actualTMRequest);
  }
}
