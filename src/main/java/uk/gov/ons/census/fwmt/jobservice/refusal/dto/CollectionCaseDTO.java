package uk.gov.ons.census.fwmt.jobservice.refusal.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CollectionCaseDTO {

  @Builder.Default
  private String foreName = "";

  @Builder.Default
  private String surname = "";

  private String isHouseholder;

}
