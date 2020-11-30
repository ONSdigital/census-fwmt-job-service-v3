package uk.gov.ons.census.fwmt.jobservice.refusal.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CaseDetailsDTO {

  @JsonProperty("id")
  private UUID caseId;

  List<CaseDetailsEventDTO> events;

  private RefusalTypeDTO refusalReceived;

}
