package uk.gov.ons.census.fwmt.jobservice.service.converter.impl;

import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.utils.JobServiceUtils;

import static uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum.AC;

@Component("AC")
public class AddressCheckConverter implements CometConverter {

  @Override
  public CaseRequest convert(CreateFieldWorkerJobRequest ingest) {
    CaseRequest caseRequest = new CaseRequest();
    Location location = new Location();

    caseRequest.setReference(ingest.getCaseReference());
    caseRequest.setType(AC);
    caseRequest.setSurveyType(ingest.getSurveyType());
    caseRequest.setCategory("Not applicable");
    caseRequest.setEstabType(ingest.getEstablishmentType());
    caseRequest.setCoordCode(ingest.getCoordinatorId());
    caseRequest.setDescription(ingest.getDescription());
    caseRequest.setSpecialInstructions(ingest.getSpecialInstructions());

    caseRequest.setAddress(JobServiceUtils.setAddress(ingest));

    location.setLat(ingest.getAddress().getLatitude().floatValue());
    location.set_long(ingest.getAddress().getLongitude().floatValue());
    caseRequest.setLocation(location);

    caseRequest.setUaa(ingest.isUua());
    caseRequest.setSai(ingest.isSai());

    return caseRequest;
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
