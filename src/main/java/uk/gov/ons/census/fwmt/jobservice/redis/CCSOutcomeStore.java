package uk.gov.ons.census.fwmt.jobservice.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.jobservice.utils.RedisUtil;

@Slf4j
@Component
public class CCSOutcomeStore {

  private final RedisUtil<String> redisUtil;

  public CCSOutcomeStore(RedisUtil<String> redisUtil) {
    this.redisUtil = redisUtil;
  }

  public String retrieveCache(String caseId) {
    String output = String.valueOf(redisUtil.getValue(caseId));
    if (output != null) {
      log.info("Received object from cache with case ID: " + caseId);
    }
    return output;
  }
}
