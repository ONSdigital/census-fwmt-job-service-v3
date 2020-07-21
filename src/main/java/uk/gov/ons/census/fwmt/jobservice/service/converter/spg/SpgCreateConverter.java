package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

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

public final class SpgCreateConverter {

  private SpgCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertSPG(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {

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
        .build();
    commonBuilder.address(outAddress);

    CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
        .ce1Complete(false)
        .deliveryRequired(false)
        .expectedResponses(0)
        .actualResponses(0)
        .build();
    commonBuilder.ce(ceCaseExtension);
  
    return commonBuilder;
  }

  public static CaseRequest convertSecureSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .reference("SECSS_" + ffu.getCaseRef())
        .description(((cache!=null && cache.getCareCodes()!=null && !cache.getCareCodes().isEmpty())?cache.getCareCodes()
            + "\n":"") + "Secure Site").build();
  }
  public static CaseRequest convertSecureUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)    
        .reference("SECSU_" + ffu.getCaseRef())
        .description(((cache!=null && cache.getCareCodes()!=null && !cache.getCareCodes().isEmpty())?cache.getCareCodes()
            + "\n":"") + "Secure Site").build();
  }

  public static CaseRequest convertSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Site)
        .build();
  }

  public static CaseRequest convertUnitDeliver(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_D)
        .build();
  }

  public static CaseRequest convertUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgCreateConverter.convertSPG(ffu, cache, CaseRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)
        .build();
  }
}