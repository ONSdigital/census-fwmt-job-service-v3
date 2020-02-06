package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.Contact from census-fwmt-gateway-common

@Data
@Builder
public class Contact {

  @Builder.Default
  private String name = "The Occupier";

  private String organisationName;

  private String phone;

  private String email;

}
