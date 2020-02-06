package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.Link from census-fwmt-gateway-common

@Data
@Builder
public class Link {

  private String rel;

  private String href;

}
