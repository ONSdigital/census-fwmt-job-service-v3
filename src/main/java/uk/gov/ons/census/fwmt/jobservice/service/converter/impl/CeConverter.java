package uk.gov.ons.census.fwmt.jobservice.service.converter.impl;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.utils.JobServiceUtils;

import static uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum.CE;

@Component("CE")
public class CeConverter implements CometConverter {

  @Override
  public CaseRequest convert(CreateFieldWorkerJobRequest ingest) {
    CaseRequest caseRequest = new CaseRequest();
    CeCaseExtension ceCaseExtension = new CeCaseExtension();
    Location location = new Location();

    caseRequest.setReference(ingest.getCaseReference());
    caseRequest.setType(CE);
    caseRequest.setSurveyType(ingest.getSurveyType());
    // Category is not yet in the feed
    caseRequest.setCategory("Not applicable");
    //    caseRequest.setCategory(ingest.getCategory());
    caseRequest.setEstabType(ingest.getEstablishmentType());
    caseRequest.setRequiredOfficer(ingest.getMandatoryResource());
    caseRequest.setCoordCode(ingest.getCoordinatorId());

    Contact contact = new Contact();
    if (ingest.getContact() != null) {
      contact.setOrganisationName(ingest.getContact().getOrganisationName());
    }
    contact.setName("The Manager");
    caseRequest.setContact(contact);

    caseRequest.setAddress(JobServiceUtils.setAddress(ingest));

    location.setLat(ingest.getAddress().getLatitude().floatValue());
    location.set_long(ingest.getAddress().getLongitude().floatValue());
    caseRequest.setLocation(location);

    caseRequest.setUaa(ingest.isUua());
    caseRequest.setSai(ingest.isSai());
    caseRequest.setBlankFormReturned(ingest.isBlankFormReturned());

    ceCaseExtension.setExpectedResponses(ingest.getCeExpectedResponses());
    ceCaseExtension.setActualResponses(ingest.getCeActualResponses());
    ceCaseExtension.setDeliveryRequired(ingest.isCeDeliveryRequired());
    ceCaseExtension.setCe1Complete(ingest.isCeCE1Complete());
    caseRequest.setCe(ceCaseExtension);

    return caseRequest;
  }

  @Override
  public CaseRequest convertUpdate(UpdateFieldWorkerJobRequest ingest, ModelCase modelCase) {
    throw new UnsupportedOperationException();
  }

  @Override
  public CasePauseRequest convertCancel(CancelFieldWorkerJobRequest cancelIngest) {
    throw new UnsupportedOperationException();
  }
}
