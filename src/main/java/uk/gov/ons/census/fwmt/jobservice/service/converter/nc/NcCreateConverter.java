package uk.gov.ons.census.fwmt.jobservice.service.converter.nc;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public class NcCreateConverter {

  private NcCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertNC(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.reference("NC" + ffu.getCaseRef());
    commonBuilder.type(CaseType.NC);
    commonBuilder.surveyType(SurveyType.NC);
    commonBuilder.category("HH");

    Geography outGeography = Geography
        .builder()
        .oa(ffu.getOa())
        .build();

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

    return commonBuilder;
  }

  public static CaseRequest convertNcEnglandAndWales(FwmtActionInstruction ffu, GatewayCache cache) {
    return NcCreateConverter
        .convertNC(ffu, cache, CaseRequest.builder())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .specialInstructions(getSpecialInstructions(cache))
        .description(getDescription(ffu, cache))
        .build();
  }

  private static String getDescription(FwmtActionInstruction ffu, GatewayCache cache) {
    StringBuilder description = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      description.append(cache.getCareCodes());
      description.append("\n");
    }
    return description.toString();
  }

  private static String getSpecialInstructions(GatewayCache cache) {
    StringBuilder instruction = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      instruction.append(cache.getCareCodes());
      instruction.append("\n");
    }
    if (cache != null && cache.getAccessInfo() != null && !cache.getAccessInfo().isEmpty()) {
      instruction.append(cache.getAccessInfo());
      instruction.append("\n");
    }
    return instruction.toString();
  }
}
