package uk.gov.ons.census.fwmt.jobservice.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Builder(toBuilder = true)
@Entity
@Table(name = "gateway_cache")
public class GatewayCache {
  @Id
  @Column(name = "case_id", unique = true, nullable = false)
  public final String caseId;

  @Column(name = "exists_in_fwmt")
  @JsonProperty("existsInFWMT")
  public final boolean existsInFwmt;

  @Column(name = "is_delivered")
  public final boolean delivered;

  @Column(name = "care_code")
  public final String careCodes;

  @Column(name = "access_info")
  public final String accessInfo;

  // display only the details related to request routing
  public String toRoutingString() {
    return "GatewayCache(" +
        "existsInFwmt=" + this.existsInFwmt + ", " +
        "delivered=" + this.delivered + ")";
  }
}
