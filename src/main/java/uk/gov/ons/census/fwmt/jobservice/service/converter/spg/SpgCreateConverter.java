package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseType;
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

    Contact.ContactBuilder outContact = Contact.builder();
    outContact.organisationName(ffu.getOrganisationName());
    builder.contact(outContact.build());

    Address.AddressBuilder outAddress = Address.builder();
    outAddress.lines(List.of(
        ffu.getAddressLine1(),
        Objects.toString(ffu.getAddressLine2(), ""),
        Objects.toString(ffu.getAddressLine3(), "")
    ));
    outAddress.town(ffu.getTownName());
    outAddress.postcode(ffu.getPostcode());
    builder.address(outAddress.build());

    Geography.GeographyBuilder outGeography = Geography.builder();
    outGeography.oa(ffu.getOa());
    outAddress.geography(outGeography.build());

    Location.LocationBuilder outLocation = Location.builder();
    outLocation.lat(ConverterUtils.parseFloat(ffu.getLatitude()));
    outLocation._long(ConverterUtils.parseFloat(ffu.getLongitude()));
    builder.location(outLocation.build());

    if (cache != null) {
      builder.description(cache.getCareCodes());
      builder.specialInstructions(cache.getAccessInfo());
    }

    builder.uaa(ffu.getUaa());
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

