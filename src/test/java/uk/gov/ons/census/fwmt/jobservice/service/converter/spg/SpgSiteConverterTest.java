package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseType;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.SpgFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateSecureSiteRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateSiteRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateUnitDeliverRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateUnitFollowupRouter;
import uk.gov.ons.census.fwmt.jobservice.spg.SpgRequestBuilder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpgSiteConverterTest {
  private final RouterList<CaseCreateRequest> router;

  public SpgSiteConverterTest() {
    SpgFollowUpSchedulingService spgFollowUpSchedulingService = Mockito.mock(SpgFollowUpSchedulingService.class);
    GatewayEventManager eventManager = Mockito.mock(GatewayEventManager.class);
    router = new RouterList<>(List.of(
        new SpgCreateSiteRouter(),
        new SpgCreateSecureSiteRouter(),
        new SpgCreateUnitDeliverRouter(),
        new SpgCreateUnitFollowupRouter(spgFollowUpSchedulingService)
    ), eventManager);
  }

  @Test
  void confirm_valid_spgRequest_can_be_converted() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    GatewayCache cache = GatewayCache.builder().build();
    assertTrue(router.isValid(ffu, cache));
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
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_actionInstruction_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setActionInstruction("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_surveyName_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setSurveyName("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressType_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressType("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_addressLevel_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    ffu.setAddressLevel("nonsense");
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_spgRequest_with_invalid_secureEstablishment_returns_false() {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSecureSite();
    GatewayCache cache = GatewayCache.builder().build();
    assertFalse(router.isValid(ffu, cache));
  }

  @Test
  void confirm_valid_spgRequest_creates_valid_TM_request() throws GatewayException {
    FieldworkFollowup ffu = SpgRequestBuilder.makeSite();
    GatewayCache cache = GatewayCache.builder()
        .build();

    CaseCreateRequest actualTMRequest = router.route(ffu, cache);

    Contact contact = Contact.builder()
        //.name("Mx exampleForename exampleSurname")
        .organisationName("exampleOrgName")
        //.phone("examplePhoneNumber")
        //.email(null)
        .build();

    Address address = Address.builder()
        .lines(List.of("exampleAddr1", "exampleAddr2", "exampleAddr3"))
        .town("exampleTown")
        .postcode("examplePostcode").build();

    Location location = Location.builder().lat(2.0f)._long(3.0f).build();

    CaseCreateRequest expectedTMRequest = CaseCreateRequest.builder()
        .reference("exampleCaseRef")
        .type(CaseType.CE)
        .surveyType(SurveyType.SPG_Site)
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
