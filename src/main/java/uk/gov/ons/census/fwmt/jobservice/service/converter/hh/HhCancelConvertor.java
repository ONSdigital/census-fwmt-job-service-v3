package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

public final class HhCancelConvertor {

  private HhCancelConvertor(){
  }

  public static CasePauseRequest buildCancel(FwmtActionInstruction ffu) {
      return CasePauseRequest.builder()
          .code("inf")
          .build();
  }
}
