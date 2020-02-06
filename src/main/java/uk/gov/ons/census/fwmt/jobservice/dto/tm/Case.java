package uk.gov.ons.census.fwmt.jobservice.dto.tm;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase from census-fwmt-gateway-common

@Data
@Builder
public class Case {

  private UUID id;

  private String reference;

  private CaseType type;

  private String surveyType;

  private String category;

  private String estabType;

  private String coordCode;

  private Contact contact;

  private Address address;

  private Location location;

  private String description;

  private String specialInstructions;

  @Builder.Default private boolean uaa = false;

  @Builder.Default private boolean blankFormReturned = false;

  @Builder.Default private boolean sai = false;

  private CeCaseExtension ce;

  private CcsCaseExtension ccs;

  private CasePause pause;

  private List<Link> _links;

}

