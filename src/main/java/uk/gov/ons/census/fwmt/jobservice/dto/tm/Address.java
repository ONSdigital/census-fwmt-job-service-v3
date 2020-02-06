package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.Address from census-fwmt-gateway-common

@Data
@Builder
public class Address {

  private Long uprn;

  private List<String> lines;

  private String town;

  private String postcode;

  private Geography geography;

  private String addressType;

  private String estabType;

  private String orgName;

}

