package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

public final class HhCancelConverter {

  private HhCancelConverter(){
  }

  public static CasePauseRequest buildCancel(FwmtCancelActionInstruction ffu) {
      return CasePauseRequest.builder()
          .code("inf")
          .build();
  }
}
