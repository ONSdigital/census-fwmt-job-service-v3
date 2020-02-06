package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.CasePause from census-fwmt-gateway-common
// This class is also a copy of uk.gov.ons.census.fwmt.common.data.modelcase.AbsolutePauseRequest from census-fwmt-gateway-common

@Data
@Builder
public class CasePause {

  private OffsetDateTime until;

  private String reason;

}
