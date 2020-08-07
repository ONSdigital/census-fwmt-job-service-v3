package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

public final class HhPauseConverter {

  private HhPauseConverter() {
  }

  public static CasePauseRequest buildPause(FwmtActionInstruction ffu, GatewayCache cache) {
    return CasePauseRequest.builder()
        .code(ffu.getPauseCode())
//        .effectiveFrom() OFFSET TIME ... yep
        .reason(ffu.getPauseReason())
        .build();
  }
}
