package uk.gov.ons.census.fwmt.jobservice.refusal.dto;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class CaseDetailsEventDTO {

  private UUID id;

  private String eventType;

  private OffsetDateTime eventDate;

  private String eventPayload;
}
