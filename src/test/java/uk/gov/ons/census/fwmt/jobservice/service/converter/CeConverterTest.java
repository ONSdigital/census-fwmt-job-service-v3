package uk.gov.ons.census.fwmt.jobservice.service.converter;

import ma.glasnost.orika.MapperFacade;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.service.converter.impl.CeConverter;
import uk.gov.ons.census.fwmt.jobservice.helper.FieldWorkerJobRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.service.comet.CometRestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CeConverterTest {

  @InjectMocks
  private CeConverter ceConverter;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private ModelCase modelCase;

  @Mock
  private MapperFacade mapperFacade;

  @Mock
  private CaseRequest caseRequest;

  @Test
  @Disabled
  public void createConvertRequest() throws GatewayException {
    // Given
    CreateFieldWorkerJobRequest createFieldWorkerJobRequest = new FieldWorkerJobRequestBuilder()
        .createFieldWorkerCEJobRequestForConvert();

    // When
    CaseRequest caseRequest = ceConverter.convert(createFieldWorkerJobRequest);

    // Then
    assertEquals(createFieldWorkerJobRequest.getCaseReference(), caseRequest.getReference());
    assertEquals("CE", caseRequest.getType().toString());
    assertEquals(createFieldWorkerJobRequest.getMandatoryResource(), caseRequest.getRequiredOfficer());

  }
}
