package uk.gov.ons.census.fwmt.jobservice.refusal.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectionCaseDTO {

  @Builder.Default
  private String foreName = "";

  @Builder.Default
  private String surname = "";

  private String isHouseholder;

}
