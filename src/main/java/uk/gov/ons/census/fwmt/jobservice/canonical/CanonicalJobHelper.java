package uk.gov.ons.census.fwmt.jobservice.canonical;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.ons.census.fwmt.canonical.v1.Address;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.Contact;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.ctp.response.action.message.instruction.ActionAddress;
import uk.gov.ons.ctp.response.action.message.instruction.ActionContact;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;
import uk.gov.ons.ctp.response.action.message.instruction.ActionRequest;
import uk.gov.ons.ctp.response.action.message.instruction.ActionUpdate;

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.UUID;

import static uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum.HH;

@Component
public class CanonicalJobHelper {

  private static final String CANCEL_ACTION_TYPE = "Cancel";
  private static final String CANCEL_REASON = "HQ Case Closure";
  private static final String CANCEL_PAUSE_END_DATE = "2030-01-01T00:00+00:00";
  private static final String CREATE_ACTION_TYPE = "Create";
  private static final String UPDATE_ACTION_TYPE = "Update";

  private String ccsIntUrl;

  @Value("${equrl}")
  private String setCcsIntUrl(String ccsIntUrl) {
    return this.ccsIntUrl = ccsIntUrl;
  }

  public CreateFieldWorkerJobRequest newCreateJob(ActionInstruction actionInstruction) throws GatewayException {
    String country = "";
    CreateFieldWorkerJobRequest createJobRequest = new CreateFieldWorkerJobRequest();
    ActionRequest actionRequest = actionInstruction.getActionRequest();
    ActionAddress actionAddress = actionRequest.getAddress();
    ActionContact actionContact = actionRequest.getContact();
    if (!actionInstruction.getActionRequest().getSurveyName().equals("CCS")) {
      country = actionAddress.getOa().substring(0, 1);
    }

    createJobRequest.setGatewayType(CREATE_ACTION_TYPE);
    createJobRequest.setCaseId(UUID.fromString(actionRequest.getCaseId()));
    createJobRequest.setCaseReference(actionRequest.getCaseRef());
    createJobRequest.setCaseType(processCaseType(actionRequest));
    createJobRequest.setSurveyType(processSurveyType(actionRequest));

    if (createJobRequest.getSurveyType().equals("CCS INT")) {
      createJobRequest.setCcsQuestionnaireURL(ccsIntUrl + actionRequest.getCaseId());
    }

    createJobRequest.setCategory(processCategory(actionRequest));
    createJobRequest.setEstablishmentType(actionAddress.getEstabType());

    if (country.equals("N")) {
      if (!StringUtils.isEmpty(actionRequest.getFieldOfficerId())) {
        createJobRequest.setMandatoryResource(processMandatoryResource(actionRequest));
      } else {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR,
            "A NISRA request was sent but did not include a field officer ID and/or coordinator ID for case {}",
            actionRequest.getCaseId());
      }
    }

    // Coordinator ID should always be present but if it's not then a null pointer exception would be thrown. Added a
    if (!StringUtils.isEmpty(actionRequest.getCoordinatorId())) {
      createJobRequest.setCoordinatorId(actionRequest.getCoordinatorId());
    } else {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR,
          "A case request was sent that did not include a coordinator ID for case {}",
          actionRequest.getCaseId());
    }
    createJobRequest.setActionType(actionRequest.getActionType());

    if (!StringUtils.isEmpty(actionContact)) {
      Contact contact = getContact(actionContact, actionAddress);
      createJobRequest.setContact(contact);
    }

    Address address = buildAddress(actionAddress, createJobRequest.getSurveyType());
    createJobRequest.setAddress(address);

    createJobRequest.setUua(actionRequest.isUndeliveredAsAddress());
    createJobRequest.setBlankFormReturned(actionRequest.isBlankQreReturned());

    processShelteredAccommodationIndicator(createJobRequest, actionAddress);

    if (actionRequest.getSurveyName().equals("CENSUS")) {
      setCeDetail(createJobRequest, actionRequest);
    }

    return createJobRequest;
  }

  private void setCeDetail(CreateFieldWorkerJobRequest createJobRequest, ActionRequest actionRequest) {
    if (actionRequest.getAddressType().equals("CE")) {
      createJobRequest.setCeDeliveryRequired(actionRequest.isCeDeliveryReqd());
      createJobRequest.setCeCE1Complete(actionRequest.isCeCE1Complete());
      createJobRequest.setCeExpectedResponses(actionRequest.getCeExpectedResponses().intValue());
      createJobRequest.setCeActualResponses(actionRequest.getCeActualResponses().intValue());
    }
  }

  private Contact getContact(ActionContact actionContact, ActionAddress actionAddress) {
    Contact contact = new Contact();
    contact.setForename(actionContact.getForename());
    contact.setSurname(actionContact.getSurname());
    contact.setOrganisationName(actionAddress.getOrganisationName());
    contact.setPhoneNumber(actionContact.getPhoneNumber());
    contact.setEmailAddress(actionContact.getEmailAddress());

    return contact;
  }

  private Address buildAddress(ActionAddress actionAddress, String surveyType) {
    Address address = new Address();
    address.setArid(actionAddress.getArid());
    address.setUprn(actionAddress.getUprn());
    address.setLine1(actionAddress.getLine1());
    address.setLine2(actionAddress.getLine2());
    address.setLine3(actionAddress.getLine3());
    address.setTownName(actionAddress.getTownName());
    address.setPostCode(actionAddress.getPostcode());
    address.setLatitude(actionAddress.getLatitude());
    address.setLongitude(actionAddress.getLongitude());
    if (!surveyType.equals("CCS INT")) {
      address.setOa(actionAddress.getOa());
    }

    return address;
  }

  private String processMandatoryResource(ActionRequest actionRequest) {
    switch (actionRequest.getAddressType()) {
    case "HH":
      if (!StringUtils.isEmpty(actionRequest.getFieldOfficerId())) {
        return actionRequest.getFieldOfficerId();
      } else {
        break;
      }
    case "CE":
      return actionRequest.getFieldOfficerId();
    case "CSS":
      break;
    }
    return null;
  }

  private String processCaseType(ActionRequest actionRequest) throws GatewayException {
    String addressType = actionRequest.getAddressType();
    String surveyName = actionRequest.getSurveyName();

    switch (surveyName) {
    case "CENSUS":
      switch (addressType) {
      case "HH":
        return "HH";
      case "CE":
      case "SPG":
        return "CE";
      default:
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Non-valid addressType: " + addressType);
      }
    case "CCS":
      return "CCS";
    default:
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Invalid survey name: " + surveyName);
    }
  }

  private String processSurveyType(ActionRequest actionRequest) throws GatewayException {
    String addressType = actionRequest.getAddressType();
    String addressLevel = actionRequest.getAddressLevel();
    String surveyName = actionRequest.getSurveyName();

    switch (surveyName) {
    case "CENSUS":
      if (addressType.equals("HH")) {
        return "Household";
      } else if (addressType.equals("CE") && addressLevel.equals("E")) {
        return "CE EST";
      } else if (addressType.equals("CE") && addressLevel.equals("U")) {
        return "CE UNIT";
      } else if (addressType.equals("SPG")) {
        return "CE SPG";

      } else {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Unable to set survey type using "
            + addressType + " and " + addressLevel);
      }
    case "CCS":
      return "CCS INT";
    default:
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Unable to set survey type using "
          + addressType + " and " + addressLevel + "and" + surveyName);
    }
  }

  private String processCategory(ActionRequest actionRequest) {
    String surveyName = actionRequest.getSurveyName();
    String addressType = actionRequest.getAddressType();

    if (surveyName.equals("CCS")) {
      switch (addressType) {
      case "HH":
        return "Household";
      case "CE":
        return "CE";
      case "SPG":
        return "CCS";
      }
    }
    // only required to be something in CCS?
    return null;
  }

  private void processShelteredAccommodationIndicator(CreateFieldWorkerJobRequest createJobRequest,
      ActionAddress actionAddress) {
    boolean sai = String.valueOf(actionAddress.getType()).equals(String.valueOf(HH)) && actionAddress.getEstabType()
        .equals("Sheltered Accommodation");
    createJobRequest.setSai(sai);
  }

  private Date convertXmlGregorianCalendarToDate(XMLGregorianCalendar xmlGregorianCalendar) {
    GregorianCalendar cal = xmlGregorianCalendar.toGregorianCalendar();

    return cal.getTime();
  }

  private OffsetDateTime convertXmlGregorianToOffsetDateTime(XMLGregorianCalendar xmlGregorianCalendar) {
    GregorianCalendar cal = xmlGregorianCalendar.toGregorianCalendar();
    Date date = cal.getTime();

    return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
  }

  public CancelFieldWorkerJobRequest newCancelJob(ActionInstruction actionInstruction) {
    CancelFieldWorkerJobRequest cancelJobRequest = new CancelFieldWorkerJobRequest();
    cancelJobRequest.setGatewayType(CANCEL_ACTION_TYPE);
    if (actionInstruction.getActionCancel().getAddressType().equals("HH")) {
      createIndefinitePause(cancelJobRequest, actionInstruction);
    }
    cancelJobRequest.setCaseId(UUID.fromString(actionInstruction.getActionCancel().getCaseId()));

    return cancelJobRequest;
  }

  private void createIndefinitePause(CancelFieldWorkerJobRequest cancelJobRequest,
      ActionInstruction actionInstruction) {
    cancelJobRequest.setReason(CANCEL_REASON);
    cancelJobRequest.setUntil(OffsetDateTime.parse(CANCEL_PAUSE_END_DATE));
  }

  public UpdateFieldWorkerJobRequest newUpdateJob(ActionInstruction actionInstruction) throws GatewayException {
    ActionUpdate actionUpdate = actionInstruction.getActionUpdate();

    UpdateFieldWorkerJobRequest updateJobRequest = new UpdateFieldWorkerJobRequest();
    updateJobRequest.setGatewayType(UPDATE_ACTION_TYPE);
    updateJobRequest.setCaseId(UUID.fromString(actionUpdate.getCaseId()));
    updateJobRequest.setAddressType(actionUpdate.getAddressType());
    updateJobRequest.setAddressLevel(actionUpdate.getAddressLevel());
    updateJobRequest.setUaa(actionUpdate.isUndeliveredAsAddress());

    if (!StringUtils.isEmpty(actionUpdate.getActionableFrom())) {
      if (!actionInstruction.getActionUpdate().getAddressType().equals("CCS PL")) {
        updateJobRequest.setHoldUntil(convertXmlGregorianToOffsetDateTime(actionUpdate.getActionableFrom()));
      } else {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR,
            "A case of type CCS cannot be paused for case ID: "
                + actionUpdate.getCaseId());
      }
    }

    updateJobRequest.setCe1Complete(actionUpdate.isCe1Complete());
    updateJobRequest.setCeExpectedResponses(actionUpdate.getCeExpectedResponses().intValue());
    updateJobRequest.setCeActualResponses(actionUpdate.getCeActualResponses().intValue());
    updateJobRequest.setBlankFormReturned(actionUpdate.isBlankQreReturned());

    return updateJobRequest;
  }
}
