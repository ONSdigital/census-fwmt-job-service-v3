package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class SpgUpdateConverter {

  private SpgUpdateConverter() {
  }

  private static CaseReopenCreateRequest.CaseReopenCreateRequestBuilder convertCommon(FwmtActionInstruction ffu,
      GatewayCache cache) {
    return CaseReopenCreateRequest.builder().id(ffu.getCaseId());
  }

  public static CaseReopenCreateRequest convertSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgUpdateConverter.convertCommon(ffu, cache).build();
  }

  public static CaseReopenCreateRequest convertUnit(FwmtActionInstruction ffu, GatewayCache cache) {
    return SpgUpdateConverter.convertCommon(ffu, cache)
        .surveyType(SurveyType.SPG_Unit_F)
        .uaa(ffu.isUndeliveredAsAddress())
        .blankFormReturned(ffu.isBlankFormReturned())
        .build();
  }
}

