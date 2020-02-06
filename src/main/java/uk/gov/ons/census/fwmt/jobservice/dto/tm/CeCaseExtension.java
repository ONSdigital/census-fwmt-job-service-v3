package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension from census-fwmt-gateway-common

@Data
@Builder
public class CeCaseExtension {

  @Builder.Default private Boolean ce1Complete = false;

  @Builder.Default private Boolean deliveryRequired = false;

  private Integer expectedResponses;

  private Integer actualResponses;

}
