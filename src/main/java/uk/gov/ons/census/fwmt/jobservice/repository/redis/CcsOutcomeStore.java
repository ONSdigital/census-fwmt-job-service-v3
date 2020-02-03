package uk.gov.ons.census.fwmt.jobservice.repository.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CcsOutcomeStore {

  private final RedisUtil<String> redisUtil;

  public CcsOutcomeStore(RedisUtil<String> redisUtil) {
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
