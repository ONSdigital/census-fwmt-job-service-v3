package uk.gov.ons.census.fwmt.jobservice.service.converter.hh;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class HhCancelConverter {

  private HhCancelConverter(){
  }

  public static CasePauseRequest buildCancel(FwmtCancelActionInstruction ffu) {
    Date currentDate = null;
    SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss z yyyy", Locale.ENGLISH);
    try {
      currentDate = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
    } catch (ParseException e) {
      String error = e.toString();
      System.out.println(error);
    }
    
    return CasePauseRequest.builder()
          .code("inf")
          .effectiveFrom(currentDate)
          .build();
  }
}
