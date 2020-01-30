package uk.gov.ons.census.fwmt.jobservice.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.jobservice.config.RedisUtil;

@Slf4j
@Component
public class HouseholdStore {

  @Autowired
  private RedisUtil<HouseholdRequestEntity> redisUtil;

  public HouseholdRequestEntity cacheJob(String caseId) {
    HouseholdRequestEntity householdRequestEntity = new HouseholdRequestEntity();

    householdRequestEntity.setCaseId(caseId);

    redisUtil.putValue(householdRequestEntity.getCaseId(), householdRequestEntity);
    return householdRequestEntity;
  }

  public HouseholdRequestEntity retrieveCache(String caseId) {
    HouseholdRequestEntity householdRequestEntity = redisUtil.getValue(caseId);
    if (householdRequestEntity != null) {
      log.info("Received object from cache: " + householdRequestEntity.toString());
    }
    return householdRequestEntity;
  }
}
