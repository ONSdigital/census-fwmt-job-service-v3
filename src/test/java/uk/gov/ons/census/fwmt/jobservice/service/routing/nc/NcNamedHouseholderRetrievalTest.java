package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventHardRefusal;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.helper.NcCaseDetailsDtoBuilder;
import uk.gov.ons.census.fwmt.jobservice.nc.utils.NamedHouseholderRetrieval;

import java.net.URI;
import java.util.List;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NcNamedHouseholderRetrievalTest {

  @InjectMocks
  private NamedHouseholderRetrieval namedHouseholderRetrieval;

  @Spy
  private ObjectMapper objectMapper;

  @Mock
  private URI uri;

  @Mock
  private GatewayEventManager eventManager;

  @Test
  @SuppressWarnings("unchecked")
  @DisplayName("When a empty name is received, we should send an empty name")
  public void shouldHandleIncorrectSurveyTypeCE() throws GatewayException, JsonProcessingException {
    final CaseDetailsDTO caseDetailsDTO = new NcCaseDetailsDtoBuilder().createNcCaseDetailsDto();
    final CaseDetailsEventHardRefusal caseDetailsEventHardRefusal = new NcCaseDetailsDtoBuilder().createCaseDetailsEventHardRefusal();
    List<CaseDetailsEventDTO> caseDetailsEventDTO = caseDetailsDTO.getEvents();
    ReflectionTestUtils.setField(namedHouseholderRetrieval, "privateKey", "test/resources/testPrivateKey.private");
    when(objectMapper.readValue(caseDetailsEventDTO.get(0).getEventPayload(), CaseDetailsEventHardRefusal.class)).thenReturn(caseDetailsEventHardRefusal);
    String returnedHouseholder = namedHouseholderRetrieval.getAndSortRmRefusalCases(caseDetailsDTO.getCaseId().toString(), caseDetailsDTO);
    Assertions.assertEquals("Named householder = No", returnedHouseholder);
  }
}