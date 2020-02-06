package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.Location from census-fwmt-gateway-common

@Data
@Builder
public class Location {

  private Float lat;

  @JsonProperty("long")
  private Float _long;

}
