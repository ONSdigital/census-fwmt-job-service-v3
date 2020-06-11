package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class is a caching service that reduces the frequency of date checks
 */

@Slf4j
@Service
public class CeFollowUpSchedulingService {
  @Value("${ce.followUpDate}")
  Date followUpDate;
  @Value("${ce.startDate}")
  Date startDate;
  private boolean inFollowUp = false;


  public boolean isInFollowUp() {
    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
    Date todaysDate = new Date();

    if (todaysDate.after(startDate) && todaysDate.after(followUpDate)) {
      return inFollowUp = true;
    }
     return inFollowUp;
  }

//  public void checkTimeDate() {
//    long unixTime = System.currentTimeMillis() / 1000L;
//    log.info("The time is now {}", unixTime);
//    inFollowUp = unixTime > followUpDate;
//  }
}
