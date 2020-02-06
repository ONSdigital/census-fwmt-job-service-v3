package uk.gov.ons.census.fwmt.jobservice.service.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.jobservice.consumer.message.MessageConverter;
import uk.gov.ons.census.fwmt.jobservice.helper.FieldWorkerJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.repository.redis.CcsOutcomeStore;
import uk.gov.ons.census.fwmt.jobservice.service.converter.impl.CcsPlConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CcsPlConverterTest {

  @InjectMocks
  private CcsPlConverter ccsplConverter;

  @Mock
  private CcsOutcomeStore ccsOutcomeStore;

  @Mock
  private MessageConverter messageConverter;

  @Mock
  private ObjectMapper objectMapper;

  @Test
  @Disabled
  public void createConvertRequest() {
    // Given
    CreateFieldWorkerJobRequest createFieldWorkerJobRequest = new FieldWorkerJobRequestBuilder()
        .createFieldWorkerCCSPLJobRequestForConvert();

    // When
    CaseRequest caseRequest = ccsplConverter.convert(createFieldWorkerJobRequest);

    // Then
    assertEquals(createFieldWorkerJobRequest.getCaseReference(), caseRequest.getReference());
    assertEquals("CCS", caseRequest.getType().toString());
    assertEquals(createFieldWorkerJobRequest.getMandatoryResource(), caseRequest.getRequiredOfficer());

  }
}
