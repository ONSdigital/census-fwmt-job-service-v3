package uk.gov.ons.census.fwmt.jobservice.service.converter.common;

import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class CommonSwitchConverter {

  private CommonSwitchConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(FwmtActionInstruction ffu,
      GatewayCache cache) {
    return ReopenCaseRequest.builder().id(ffu.getCaseId());
  }

  public static ReopenCaseRequest convertSite(FwmtActionInstruction ffu, GatewayCache cache) {
    return CommonSwitchConverter.convertCommon(ffu, cache)
        .surveyType(SurveyType.CE_SITE)
        .build();
  }

  public static ReopenCaseRequest convertUnitDeliver(FwmtActionInstruction ffu, GatewayCache cache) {
    return CommonSwitchConverter.convertCommon(ffu, cache)
        .surveyType(SurveyType.CE_EST_D)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }

  public static ReopenCaseRequest convertUnitFollowup(FwmtActionInstruction ffu, GatewayCache cache) {
    return CommonSwitchConverter.convertCommon(ffu, cache)
        .surveyType(SurveyType.CE_EST_F)
        .uaa(ffu.isUndeliveredAsAddress())
        .blank(ffu.isBlankFormReturned())
        .build();
  }
}

