package uk.gov.ons.census.fwmt.jobservice.service.converter.impl;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.Address;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.ccs.CCSPropertyListingOutcome;
import uk.gov.ons.census.fwmt.common.data.ccs.CareCode;
import uk.gov.ons.census.fwmt.common.data.ccs.CeDetails;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CcsCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.consumer.message.MessageConverter;
import uk.gov.ons.census.fwmt.jobservice.repository.redis.CcsOutcomeStore;

import java.util.ArrayList;
import java.util.List;

import static uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum.CCS;
import static uk.gov.ons.census.fwmt.jobservice.utils.JobServiceUtils.setAddress;

@Component("CCS")
public class CcsIntConverter implements CometConverter {

  private final CcsOutcomeStore ccsOutcomeStore;
  private final MessageConverter messageConverter;

  public CcsIntConverter(
      CcsOutcomeStore ccsOutcomeStore,
      MessageConverter messageConverter) {
    this.ccsOutcomeStore = ccsOutcomeStore;
    this.messageConverter = messageConverter;
  }

  @Override
  public CaseRequest convert(CreateFieldWorkerJobRequest ingest) throws GatewayException {

    CcsCaseExtension ccsCaseExtension = new CcsCaseExtension();
    CCSPropertyListingOutcome ccsPropertyListingCached = getCachedOutcomeDetails(ingest);
    CaseRequest caseRequest = new CaseRequest();
    Location location = new Location();

    caseRequest.setReference(ingest.getCaseReference());
    caseRequest.setType(CCS);
    caseRequest.setSurveyType(ingest.getSurveyType());
    caseRequest.setCategory(ingest.getCategory());
    caseRequest.setRequiredOfficer(ccsPropertyListingCached.getUsername());
    caseRequest.setCoordCode(ingest.getCoordinatorId());
    caseRequest.setContact(setContact(ingest));

    if (ccsPropertyListingCached.getPrimaryOutcome().equals("CE")) {
      caseRequest.setEstabType("CE");
    } else {
      caseRequest.setEstabType("HH");
      ccsCaseExtension.setQuestionnaireUrl(ingest.getCcsQuestionnaireURL());
    }

    location.setLat(ingest.getAddress().getLatitude().floatValue());
    location.set_long(ingest.getAddress().getLongitude().floatValue());
    caseRequest.setLocation(location);
    caseRequest.setCcs(ccsCaseExtension);

    ingest.setAddress(updateAddressWithOa(ingest, ccsPropertyListingCached.getAddress().getOa()));

    caseRequest.setSpecialInstructions(getAdditionalInformation(ccsPropertyListingCached, true));
    caseRequest.setDescription(getAdditionalInformation(ccsPropertyListingCached, false));
    caseRequest.setAddress(setAddress(ingest));
    return caseRequest;
  }

  private Contact setContact(CreateFieldWorkerJobRequest ingest) {
    Contact contact = new Contact();
    contact.setName(ingest.getAddress().getPostCode());
    return contact;
  }

  private CCSPropertyListingOutcome getCachedOutcomeDetails(CreateFieldWorkerJobRequest ingest)
      throws GatewayException {
    String retrievedCache = ccsOutcomeStore.retrieveCache(String.valueOf(ingest.getCaseId()));

    return messageConverter.convertMessageToDTO(CCSPropertyListingOutcome.class, retrievedCache);
  }

  private String getAdditionalInformation(CCSPropertyListingOutcome cachedSpecialInstructions,
      boolean isSpecialInstruction) {
    List<String> additionalInformation = new ArrayList<>();
    String additionalInformationToString;
    String accessInfo;
    String careCode;
    String ceDetails;

    if (isSpecialInstruction) {
      if (cachedSpecialInstructions.getAccessInfo() != null) {
        accessInfo = cachedSpecialInstructions.getAccessInfo();
      } else {
        accessInfo = "None";
      }
      additionalInformation.add("Access Info: " + accessInfo);
      if (cachedSpecialInstructions.getCareCodes() != null &&
          !cachedSpecialInstructions.getCareCodes().isEmpty()) {
        careCode = formatCareCodeList(cachedSpecialInstructions.getCareCodes());
        additionalInformation.add("CareCodes: " + careCode);
      } else {
        additionalInformation.add("CareCode: None");
      }
      if (cachedSpecialInstructions.getCeDetails() != null) {
        ceDetails = getCEDetails(cachedSpecialInstructions.getCeDetails());
        additionalInformation.add("CE Details: " + ceDetails);
      }
    } else {
      additionalInformation.add("Primary Outcome: " + cachedSpecialInstructions.getPrimaryOutcome());
      additionalInformation.add("Secondary Outcome: " + cachedSpecialInstructions.getSecondaryOutcome());
    }

    additionalInformationToString = additionalInformation.toString();
    additionalInformationToString = additionalInformationToString.replaceAll("[\\[\\](){}]", "");
    additionalInformationToString = additionalInformationToString.replaceAll(",", "\n");

    return additionalInformationToString;
  }

  private String getCEDetails(CeDetails ceDetails) {
    String ceDetailsToreturn = "";

    if (ceDetails.getEstablishmentType() != null) {
      ceDetailsToreturn = "Establishment Type: " + ceDetails.getEstablishmentType() + "\n";
    }
    if (ceDetails.getOrganisationName() != null) {
      ceDetailsToreturn = ceDetailsToreturn + "Organisation Name: " + ceDetails.getOrganisationName() + "\n";
    }
    if (ceDetails.getManagerName() != null) {
      ceDetailsToreturn = ceDetailsToreturn + "Manager Name: " + ceDetails.getManagerName() + "\n";
    }
    if (ceDetails.getContactPhone() != null) {
      ceDetailsToreturn = ceDetailsToreturn + "Contact Phone: " + ceDetails.getContactPhone() + "\n";
    }
    if (ceDetails.getBedspaces() != null) {
      ceDetailsToreturn = ceDetailsToreturn + "Bed Spaces: " + ceDetails.getBedspaces() + "\n";
    }
    if (ceDetails.getUsualResidents() != null) {
      ceDetailsToreturn = ceDetailsToreturn + "Usual Residents: " + ceDetails.getUsualResidents() + "\n";
    }

    return ceDetailsToreturn;
  }

  private String formatCareCodeList(List<CareCode> careCode) {
    String careCodes = "";

    for (int i = 0; i <= careCode.size(); i++) {
      careCodes = careCode.toString();
      careCodes = careCodes.replaceAll("careCode=", "");
      careCodes = careCodes.replaceAll("CareCode", "");
      careCodes = careCodes.replaceAll("[\\[\\](){}]", "");
    }
    return careCodes;
  }

  private Address updateAddressWithOa(CreateFieldWorkerJobRequest ingest, String cachedOa) {
    Address updatedAddress = ingest.getAddress();
    updatedAddress.setOa(cachedOa);

    return updatedAddress;
  }

  @Override
  public CasePauseRequest convertCancel(CancelFieldWorkerJobRequest cancelIngest) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CaseRequest convertUpdate(UpdateFieldWorkerJobRequest ingest, ModelCase modelCase) {
    throw new UnsupportedOperationException();
  }
}