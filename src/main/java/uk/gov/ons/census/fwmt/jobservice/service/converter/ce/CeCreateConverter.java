package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public final class CeCreateConverter {

  private CeCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCE(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder,
      boolean isEstab) {

    boolean ce1Completed = false;
    boolean handDelivery = false;
    int actualResponse = 0;
    int expectedResponse = 0;

    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

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
    commonBuilder.address(outAddress);

    if (isEstab) {
      if (ffu.isCe1Complete()) {
        ce1Completed = true;
      }

      if (ffu.getCeActualResponses() != null && ffu.getCeActualResponses() != 0) {
        actualResponse = ffu.getCeActualResponses();
      }

      if (ffu.getCeExpectedCapacity() != null && ffu.getCeExpectedCapacity() != 0) {
        expectedResponse = ffu.getCeExpectedCapacity();
      }

      if (ffu.isHandDeliver()) {
        handDelivery = ffu.isHandDeliver();
      }
    }

      CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
          .ce1Complete(ce1Completed)
          .deliveryRequired(handDelivery)
          .expectedResponses(expectedResponse)
          .actualResponses(actualResponse)
          .build();
      commonBuilder.ce(ceCaseExtension);

    return commonBuilder;
  }

  public static CaseRequest convertCeEstabDeliver(FwmtActionInstruction ffu, GatewayCache cache) {
    return CeCreateConverter
        .convertCE(ffu, cache, CaseRequest.builder(), true)
        .surveyType(SurveyType.CE_EST_D)
        .build();
  }

  public static CaseRequest convertCeEstabDeliverSecure(FwmtActionInstruction ffu, GatewayCache cache) {
    String careCodes = "";

    if (cache != null) {
      careCodes = cache.getCareCodes();
    }

    return CeCreateConverter.convertCE(ffu, cache, CaseRequest.builder(), true)
        .surveyType(SurveyType.CE_EST_D)
        .reference("SECCE_" + ffu.getCaseRef())
        .description(careCodes + "\nSecure Establishment").build();
  }

  public static CaseRequest convertCeEstabFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return CeCreateConverter
        .convertCE(ffu, cache, CaseRequest.builder(), true)
        .surveyType(SurveyType.CE_EST_F)
        .build();
  }

  public static CaseRequest convertCeEstabFollowupSecure(FwmtActionInstruction ffu, GatewayCache cache)  {
    String careCodes = "";

    if (cache != null) {
      careCodes = cache.getCareCodes();
    }

    return CeCreateConverter.convertCE(ffu, cache, CaseRequest.builder(), true)
        .surveyType(SurveyType.CE_EST_F)
        .reference("SECCE_" + ffu.getCaseRef())
        .description(careCodes + "\nSecure Establishment").build();
  }

  public static CaseRequest convertCeSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return CeCreateConverter
        .convertCE(ffu, cache, CaseRequest.builder(), false)
        .surveyType(SurveyType.CE_SITE)
        .build();
  }

  public static CaseRequest convertCeSiteSecure(FwmtActionInstruction ffu, GatewayCache cache) {
    String careCodes = "";

    if (cache != null ) {
      careCodes = cache.getCareCodes();
    }

    return CeCreateConverter.convertCE(ffu, cache, CaseRequest.builder(), false)
        .surveyType(SurveyType.CE_SITE)
        .reference("SECCS_" + ffu.getCaseRef())
        .description(careCodes + "\nSecure Site").build();
  }

  public static CaseRequest convertCeUnitDeliver(FwmtActionInstruction ffu, GatewayCache cache) {
    return CeCreateConverter
        .convertCE(ffu, cache, CaseRequest.builder(), false)
        .surveyType(SurveyType.CE_UNIT_D)
        .build();
  }

  public static CaseRequest convertCeUnitDeliverSecure(FwmtActionInstruction ffu, GatewayCache cache) {
    String careCodes = "";

    if (cache != null ) {
      careCodes = cache.getCareCodes();
    }

    return CeCreateConverter.convertCE(ffu, cache, CaseRequest.builder(), false)
        .surveyType(SurveyType.CE_UNIT_D)
        .reference("SECCU_" + ffu.getCaseRef())
        .description(careCodes + "\nSecure Unit").build();
  }

  public static CaseRequest convertCeUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return CeCreateConverter
        .convertCE(ffu, cache, CaseRequest.builder(), false)
        .surveyType(SurveyType.CE_UNIT_F)
        .build();
  }

  public static CaseRequest convertCeUnitFollowupSecure(FwmtActionInstruction ffu, GatewayCache cache) {
    String careCodes = "";

    if (cache != null ) {
      careCodes = cache.getCareCodes();
    }

    return CeCreateConverter.convertCE(ffu, cache, CaseRequest.builder(), false)
        .surveyType(SurveyType.CE_UNIT_F)
        .reference("SECCU_" + ffu.getCaseRef())
        .description(careCodes + "\nSecure Unit").build();
  }
}

