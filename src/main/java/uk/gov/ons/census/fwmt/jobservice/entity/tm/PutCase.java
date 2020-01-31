package uk.gov.ons.census.fwmt.jobservice.entity.tm;

// This class is a copy of uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest from census-fwmt-gateway-common

import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CcsCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;

/**
 * Case
 */
@Data
@NoArgsConstructor
public class PutCase {

  private String reference;

  private TypeEnum type;

  private String surveyType;

  private String category;

  private String estabType;

  private String requiredOfficer;

  private String coordCode;

  private Contact contact;

  private Address address;

  private Location location;

  private String description;

  private String specialInstructions;

  private boolean uaa = false;

  private boolean blankFormReturned = false;

  private boolean sai = false;

  private CeCaseExtension ce;

  private CcsCaseExtension ccs;

  private CasePauseRequest pause;

  /**
   * Case Type.
   */
  public enum TypeEnum {
    HH,
    CE,
    CCS,
    AC
  }

}
