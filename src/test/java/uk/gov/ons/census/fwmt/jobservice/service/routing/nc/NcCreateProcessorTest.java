package uk.gov.ons.census.fwmt.jobservice.service.routing.nc;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.NcActionInstructionBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class NcCreateProcessorTest {

  @InjectMocks
  private NcHhCreateEnglandAndWales ncHhCreateEnglandAndWales;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayCacheService cacheService;

  @Mock
  private GatewayCache gatewayCache;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RmRestClient rmRestClient;

  @Mock
  private RoutingValidator routingValidator;

  @Captor
  private ArgumentCaptor<GatewayCache> spiedCache;

  @Test
  @DisplayName("Should save the oldCaseId")
  public void shouldHandleIncorrectSurveyTypeCE() throws GatewayException {
    final FwmtActionInstruction instruction = new NcActionInstructionBuilder().createNcActionInstruction();
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendCreate(any(CaseRequest.class), eq(instruction.getCaseId()))).thenReturn(responseEntity);
    ncHhCreateEnglandAndWales.process(instruction, null, Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String originalCaseId = spiedCache.getValue().originalCaseId;
    Assertions.assertEquals(instruction.getOldCaseId(), originalCaseId);
  }

  @Test
  @DisplayName("Should not error if a null refusal value is present")
  public void shoudlHandleNullRefusalValue() throws GatewayException {
    final FwmtActionInstruction instruction = new NcActionInstructionBuilder().createNcActionInstruction();
    final CaseDetailsDTO caseDetailsDTO = new CaseDetailsDTO();
    when(rmRestClient.getCase(instruction.getOldCaseId())).thenReturn(caseDetailsDTO);
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendCreate(any(CaseRequest.class), eq(instruction.getCaseId()))).thenReturn(responseEntity);
    Assertions.assertDoesNotThrow(() -> {
      ncHhCreateEnglandAndWales.process(instruction, null, Instant.now());
    });
  }
}