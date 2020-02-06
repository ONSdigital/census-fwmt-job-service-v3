package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import lombok.Builder;
import lombok.Data;

// This class is also a copy of uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest from census-fwmt-gateway-common

@Data
@Builder
public class PauseCaseRequest {

  private String id;

  @JsonUnwrapped
  private CasePause casePause;

}
