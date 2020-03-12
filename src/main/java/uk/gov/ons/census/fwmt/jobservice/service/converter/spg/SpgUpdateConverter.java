package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class SpgUpdateConverter {

  private SpgUpdateConverter() {
  }

  public static CaseReopenCreateRequest.CaseReopenCreateRequestBuilder convertCommon(FieldworkFollowup ffu,
      GatewayCache cache, CaseReopenCreateRequest.CaseReopenCreateRequestBuilder builder) {
    return builder.id(ffu.getCaseId());
  }

  public static CaseReopenCreateRequest convertSite(FieldworkFollowup ffu, GatewayCache cache) {
    return SpgUpdateConverter.convertCommon(ffu, cache, CaseReopenCreateRequest.builder()).build();
  }

  public static CaseReopenCreateRequest convertUnit(FieldworkFollowup ffu, GatewayCache cache) {
    return SpgUpdateConverter.convertCommon(ffu, cache, CaseReopenCreateRequest.builder())
        .surveyType(SurveyType.SPG_Unit_F)
        .uaa(ffu.getUaa())
        .blankFormReturned(ffu.getBlankQreReturned())
        .build();
  }
}

