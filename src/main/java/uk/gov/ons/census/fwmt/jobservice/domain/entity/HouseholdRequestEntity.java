package uk.gov.ons.census.fwmt.jobservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@RedisHash("HouseholdRequestEntity")
public class HouseholdRequestEntity implements Serializable {

  private String caseId;

}
