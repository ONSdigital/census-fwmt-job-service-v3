package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseType;
import uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.spg.SpgCreateConverter;

import java.util.List;
import java.util.Objects;

public final class CeCreateConverter {

  private CeCreateConverter() {
  }

  public static CaseCreateRequest.CaseCreateRequestBuilder convertCommon(
      FwmtActionInstruction ffu, GatewayCache cache, CaseCreateRequest.CaseCreateRequestBuilder builder)
      throws GatewayException {

    String savedCareCodes = "";

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
        .uprn(Long.parseLong(ffu.getUprn()))
        .estabUprn(Long.parseLong(ffu.getEstabUprn()))
        .build();
    builder.address(outAddress);

    Location outLocation = Location.builder()
        .lat(ffu.getLatitude().floatValue())
        ._long(ffu.getLongitude().floatValue())
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
      savedCareCodes = cache.getCareCodes();
      builder.specialInstructions(cache.getAccessInfo());
    }

    if (ffu.isSecureEstablishment())
    {
        builder.reference("SECCU_" + ffu.getCaseRef());
        builder.description(savedCareCodes + "<br> Secure Site");
    } else if (!(savedCareCodes == null)) {
      builder.description(savedCareCodes);
    }

    builder.uaa(ffu.isUndeliveredAsAddress());
    builder.sai(false);

    return builder;
  }

  public static CaseCreateRequest convertCeEstabDeliver(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    return CeCreateConverter
        .convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_EST_D)
        .build();
  }

  public static CaseCreateRequest convertCeEstabDeliverSecure(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    String careCodes = "";

    if (cache != null) {
      careCodes = cache.getCareCodes();
    }

    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_EST_D)
        .reference("SECCE_" + ffu.getCaseRef())
        .description(careCodes + "<br> Secure Site").build();
  }

  public static CaseCreateRequest convertCeEstabFollowup(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    return CeCreateConverter
        .convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_EST_F)
        .build();
  }

  public static CaseCreateRequest convertCeEstabFollowupSecure(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    String careCodes = "";

    if (cache != null) {
      careCodes = cache.getCareCodes();
    }

    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_EST_F)
        .reference("SECCE_" + ffu.getCaseRef())
        .description(careCodes + "<br> Secure Site").build();
  }

  public static CaseCreateRequest convertCeUnitDeliver(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    return CeCreateConverter
        .convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_UNIT_D)
        .build();
  }

  public static CaseCreateRequest convertCeUnitDeliverSecure(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    String careCodes = "";

    if (cache != null ) {
      careCodes = cache.getCareCodes();
    }

    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_UNIT_D)
        .reference("SECCU_" + ffu.getCaseRef())
        .description(careCodes + "<br> Secure Site").build();
  }

  public static CaseCreateRequest convertCeUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache)
      throws GatewayException {
    return CeCreateConverter
        .convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_UNIT_F)
        .build();
  }

  public static CaseCreateRequest convertCeUnitFollowupSecure(FwmtActionInstruction ffu, GatewayCache cache) throws GatewayException {
    String careCodes = "";

    if (cache != null ) {
      careCodes = cache.getCareCodes();
    }

    return SpgCreateConverter.convertCommon(ffu, cache, CaseCreateRequest.builder())
        .surveyType(SurveyType.CE_UNIT_F)
        .reference("SECCU_" + ffu.getCaseRef())
        .description(careCodes + "<br> Secure Site").build();
  }
}

