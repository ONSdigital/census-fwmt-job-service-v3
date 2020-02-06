package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest from census-fwmt-gateway-common

@Data
@Builder
public class PutCaseRequest {

  @NonNull private String reference;
  private CaseType type;
  private String surveyType;
  @NonNull private String category;
  @NonNull private String estabType;
  private String requiredOfficer;
  @NonNull private String coordCode;
  private Contact contact;
  @NonNull private Address address;
  @NonNull private Location location;
  private String description;
  private String specialInstructions;

  @Builder.Default @NonNull private Boolean uaa = false;
  @Builder.Default private Boolean blankFormReturned = false;
  @Builder.Default @NonNull private Boolean sai = false;

  private CeCaseExtension ce;
  private CcsCaseExtension ccs;

  private PauseCaseRequest pause;

}
