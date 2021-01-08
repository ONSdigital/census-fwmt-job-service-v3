//package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Value;
//import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
//import uk.gov.ons.census.fwmt.common.error.GatewayException;
//import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
//import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
//import uk.gov.ons.census.fwmt.jobservice.helper.NcCaseDetailsDtoBuilder;
//import uk.gov.ons.census.fwmt.jobservice.nc.utils.NamedHouseholderRetrieval;
//import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
//import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
//
//import java.net.URI;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//
//@ExtendWith(MockitoExtension.class)
//public class NcNamedHouseholderRectrievalTest {
//
//  @InjectMocks
//  private NamedHouseholderRetrieval namedHouseholderRetrieval;
//
//  @Mock
//  private ObjectMapper objectMapper;
//
//  @Mock
//  private GatewayCacheService cacheService;
//
//  @Mock
//  private GatewayEventManager eventManager;
//
//  @Mock
//  private RoutingValidator routingValidator;
//
//  @Captor
//  private ArgumentCaptor<GatewayCache> spiedCache;
//
//  @Value("${decryption.pgp}")
//  private String privateKey;
//
//  @Test
//  @DisplayName("When a empty name is received, we should send an empty name")
//  public void shouldHandleIncorrectSurveyTypeCE() throws GatewayException {
//    final CaseDetailsDTO caseDetailsDTO = new NcCaseDetailsDtoBuilder().createNcCaseDetailsDto();
//    when(URI.create(privateKey)).thenReturn(any());
//    String returnedHouseholder = namedHouseholderRetrieval.getAndSortRmRefusalCases(caseDetailsDTO.getCaseId().toString(), caseDetailsDTO);
//    Assertions.assertEquals("", returnedHouseholder);
//  }
//}