package uk.gov.ons.census.fwmt.jobservice.repository.redis;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.jobservice.domain.entity.HouseholdRequestEntity;

@Slf4j
@Component
public class HouseholdStore {

  private final RedisUtil<HouseholdRequestEntity> redisUtil;

  public HouseholdStore(RedisUtil<HouseholdRequestEntity> redisUtil) {
    this.redisUtil = redisUtil;
  }

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
