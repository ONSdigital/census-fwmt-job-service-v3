package uk.gov.ons.census.fwmt.jobservice.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.ccs.CCSPropertyListingOutcome;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.converter.impl.CCSINTConverter;
import uk.gov.ons.census.fwmt.jobservice.redis.CCSOutcomeStore;
import uk.gov.ons.census.fwmt.jobservice.helper.CcsPropertyListedOutcomeBuilder;
import uk.gov.ons.census.fwmt.jobservice.helper.FieldWorkerJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.message.MessageConverter;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@RunWith(MockitoJUnitRunner.class)
public class CCSINTConverterTest {

  @InjectMocks
  private CCSINTConverter ccsintConverter;

  @Mock
  private CCSOutcomeStore ccsOutcomeStore;

  @Mock
  private MessageConverter messageConverter;

  @Mock
  private ObjectMapper objectMapper;

  @Test
  public void createConvertCeRequest() throws GatewayException {
    // Given
    CreateFieldWorkerJobRequest createFieldWorkerJobRequest = new FieldWorkerJobRequestBuilder()
        .createFieldWorkerCCSIVCeJobRequestForConvert();
    CCSPropertyListingOutcome ccsPropertyListingCached = new CcsPropertyListedOutcomeBuilder()
        .createCcsPropertyListingCeOutcome();

    String caseId = createFieldWorkerJobRequest.getCaseId().toString();
    String output = "Any";

    Mockito.when(ccsOutcomeStore.retrieveCache(caseId)).thenReturn(output);
    Mockito.when(messageConverter.convertMessageToDTO(any(), anyString())).thenReturn(ccsPropertyListingCached);

    // When
    CaseRequest caseRequest = ccsintConverter.convert(createFieldWorkerJobRequest);

    // Then
    assertEquals(createFieldWorkerJobRequest.getCaseReference(), caseRequest.getReference());
    assertEquals("CCS", caseRequest.getType().toString());
    assertEquals(createFieldWorkerJobRequest.getMandatoryResource(), ccsPropertyListingCached.getUsername());
  }

  @Test
  public void createConvertRequest() throws GatewayException {
    // Given
    CreateFieldWorkerJobRequest createFieldWorkerJobRequest = new FieldWorkerJobRequestBuilder()
        .createFieldWorkerCCSIVJobRequestForConvert();
    CCSPropertyListingOutcome ccsPropertyListingCached = new CcsPropertyListedOutcomeBuilder()
        .createCcsPropertyListingOutcome();

    String caseId = createFieldWorkerJobRequest.getCaseId().toString();
    String output = "Any";

    Mockito.when(ccsOutcomeStore.retrieveCache(caseId)).thenReturn(output);
    Mockito.when(messageConverter.convertMessageToDTO(any(), anyString())).thenReturn(ccsPropertyListingCached);

    // When
    CaseRequest caseRequest = ccsintConverter.convert(createFieldWorkerJobRequest);

    // Then
    assertEquals(createFieldWorkerJobRequest.getCaseReference(), caseRequest.getReference());
    assertEquals("CCS", caseRequest.getType().toString());
    assertEquals(createFieldWorkerJobRequest.getMandatoryResource(), ccsPropertyListingCached.getUsername());
  }
}
