package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.CcsCaseExtension from census-fwmt-gateway-common

@Data
@Builder
public class CcsCaseExtension {

  private String questionnaireUrl;

}
