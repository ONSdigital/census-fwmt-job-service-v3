package uk.gov.ons.census.fwmt.jobservice.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.service.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.helper.FieldWorkerJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.service.comet.CometRestClient;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CANCEL_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CREATE_SENT;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_SENT;

@RunWith(MockitoJUnitRunner.class)
public class JobServiceImplTest {

  @InjectMocks
  private JobService jobService;

  @Mock
  private GatewayEventManager gatewayEventManager;

  @Mock
  private Map<String, CometConverter> cometConverters;

  @Mock
  private CometRestClient restClient;

  @Mock
  private CometConverter cometConverter;

  @Mock
  private ModelCase modelCase;

  @Mock
  private CaseRequest caseRequest;

  @Test
  public void createConvertAndSendCreateTest() throws GatewayException {
    // Given
    CreateFieldWorkerJobRequest jobRequest = new FieldWorkerJobRequestBuilder().createFieldWorkerJobRequestForConvert();

    CaseRequest caseRequest = new CaseRequest();

    // When
    when(cometConverters.get(jobRequest.getCaseType())).thenReturn(cometConverter);
    when(cometConverter.convert(any(CreateFieldWorkerJobRequest.class))).thenReturn(caseRequest);
    when(restClient.sendRequest(any(CaseRequest.class), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    jobService.createJob(jobRequest);

    // Then
    Mockito.verify(gatewayEventManager).triggerEvent(anyString(), eq(COMET_CREATE_SENT), anyString(), anyString());
    Mockito.verify(gatewayEventManager).triggerEvent(anyString(), eq(COMET_CREATE_ACK), anyString(), anyString());

  }

  @Test
  public void pauseConvertAndSendCancelTest() throws GatewayException {
    // Given
    CancelFieldWorkerJobRequest jobRequest = new FieldWorkerJobRequestBuilder().cancelFieldWorkerJobRequest();

    CasePauseRequest casePauseRequest = new CasePauseRequest();

    // When
    when(cometConverters.get("HH")).thenReturn(cometConverter);
    when(cometConverter.convertCancel(any(CancelFieldWorkerJobRequest.class))).thenReturn(casePauseRequest);
    when(restClient.sendRequest(any(CasePauseRequest.class), anyString()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    jobService.cancelJob(jobRequest);

    // Then
    Mockito.verify(gatewayEventManager).triggerEvent(anyString(), eq(COMET_CANCEL_SENT));
    Mockito.verify(gatewayEventManager).triggerEvent(anyString(), eq(COMET_CANCEL_ACK), anyString(), anyString());

  }

  @Test
  public void updateConvertAndSendUpdateTest() throws GatewayException {
    // Given
    UpdateFieldWorkerJobRequest jobRequest = new FieldWorkerJobRequestBuilder().updateFieldWorkerJobRequestWithPause();

    CaseRequest caseRequest = new CaseRequest();
    CasePauseRequest casePauseRequest = new CasePauseRequest();
    caseRequest.setType(CaseRequest.TypeEnum.HH);
    casePauseRequest.setUntil(OffsetDateTime.parse("2019-07-27T00:00+00:00"));
    casePauseRequest.setId("a48bf28e-e7f4-4467-a9fb-e000b6a55676");
    caseRequest.setPause(casePauseRequest);

    ModelCase modelCase = new ModelCase();
    modelCase.setId(UUID.fromString("a48bf28e-e7f4-4467-a9fb-e000b6a55676"));

    // When
    when(restClient.sendRequest(any(CasePauseRequest.class), anyString()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(restClient.sendRequest(any(CaseRequest.class), anyString())).thenReturn(new ResponseEntity<>(HttpStatus.OK));
    when(cometConverters.get("HH")).thenReturn(cometConverter);
    when(restClient.getCase(anyString())).thenReturn(modelCase);
    when(cometConverter.convertUpdate(any(UpdateFieldWorkerJobRequest.class), any(ModelCase.class)))
        .thenReturn(caseRequest);

    jobService.updateJob(jobRequest);

    // Then
    Mockito.verify(gatewayEventManager).triggerEvent(anyString(), eq(COMET_UPDATE_SENT));
    Mockito.verify(restClient).sendRequest(casePauseRequest, String.valueOf(jobRequest.getCaseId()));
    Mockito.verify(gatewayEventManager).triggerEvent(anyString(), eq(COMET_UPDATE_ACK), anyString(), anyString());

  }
}
