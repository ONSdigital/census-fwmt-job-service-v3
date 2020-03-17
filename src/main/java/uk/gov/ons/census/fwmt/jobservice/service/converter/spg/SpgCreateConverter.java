package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseType;
import uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ConverterUtils;

import java.util.List;
import java.util.Objects;

public final class SpgCreateConverter {

  private SpgCreateConverter() {
  }

  public static CaseCreateRequest.CaseCreateRequestBuilder convertCommon(
      FieldworkFollowup ffu, GatewayCache cache, CaseCreateRequest.CaseCreateRequestBuilder builder)
      throws GatewayException {
    builder.reference(ffu.getCaseRef());
    builder.type(CaseType.CE);
    builder.category("Not applicable");
    builder.estabType(ffu.getEstabType());
    builder.requiredOfficer(ffu.getFieldOfficerId());
    builder.coordCode(ffu.getFieldCoordinatorId());

    Contact outContact = Contact.builder().organisationName(ffu.getOrganisationName()).build();
    builder.contact(outContact);

    Geography outGeography = Geography.builder().oa(ffu.getOa()).build();

    Address outAddress = Address.builder()
        .lines(List.of(
            ffu.getAddressLine1(),
            Objects.toString(ffu.getAddressLine2(), ""),
            Objects.toString(ffu.getAddressLine3(), "")
        ))
        .town(ffu.getTownName())
        .postcode(ffu.getPostcode())
        .geography(outGeography)
        .build();
    builder.address(outAddress);

    Location outLocation = Location.builder()
        .lat(ConverterUtils.parseFloat(ffu.getLatitude()))
        ._long(ConverterUtils.parseFloat(ffu.getLongitude()))
        .build();
    builder.location(outLocation);

    CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
        .ce1Complete(false)
        .deliveryRequired(false)
        .expectedResponses(0)
        .actualResponses(0)
        .build();
    builder.ce(ceCaseExtension);

    if (cache != null) {
      builder.description(cache.getCareCodes());
      builder.specialInstructions(cache.getAccessInfo());
    }

    builder.uaa((ffu.getUaa() != null) ? ffu.getUaa() : false);
    builder.sai(false);

    return builder;
  }

  public static CaseCreateRequest convertSecureSite(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.SPG_Secure_Site)
        .build();
  }

  public static CaseCreateRequest convertSite(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .build();
  }

  public static CaseCreateRequest convertUnitDeliver(FieldworkFollowup ffu, GatewayCache cache)
      throws GatewayException {
    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.SPG_Unit_D)
        .build();
  }

  public static CaseCreateRequest convertUnitFollowup(FieldworkFollowup ffu, GatewayCache cache)
      throws GatewayException {
    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)
        .build();
  }
}

