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
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.helper.NcActionInstructionBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NcCancelProcessorTest {

  @InjectMocks
  private NcHhCancel ncHhCancel;

  @InjectMocks
  private NcCeCancel ncCeCancel;

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

  @Mock
  private ResponseEntity<Void> responseEntity;

  @Captor
  private ArgumentCaptor<GatewayCache> spiedCache;

  @Test
  @DisplayName("Should send cancel NC HH caseId to TM")
  public void shouldHandleNCHHCancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new NcActionInstructionBuilder().createNcHhCancelInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("c66c995e-571d-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234")
        .originalCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATED").build();
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendClose(gatewayCache.caseId)).thenReturn(responseEntity);
    ncHhCancel.process(instruction, gatewayCache, Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String caseId = spiedCache.getValue().caseId;
    String lastAction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals("c66c995e-571d-11eb-ae93-0242ac130002", caseId);
    Assertions.assertEquals("CANCEL", lastAction);
  }

  @Test
  @DisplayName("Should send cancel NC CE caseId to TM")
  public void shouldHandleNCCECancel() throws GatewayException {
    final FwmtCancelActionInstruction instruction = new NcActionInstructionBuilder().createNcCeCancelInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("c66c995e-571d-11eb-ae93-0242ac130002").careCodes("Mind dog").accessInfo("1234")
        .originalCaseId("ac623e62-4f4b-11eb-ae93-0242ac130002").lastActionInstruction("CREATED").build();
    ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
    when(cometRestClient.sendClose(gatewayCache.caseId)).thenReturn(responseEntity);
    ncCeCancel.process(instruction, gatewayCache, Instant.now());
    verify(cacheService).save(spiedCache.capture());
    String caseId = spiedCache.getValue().caseId;
    String lastAction = spiedCache.getValue().lastActionInstruction;
    Assertions.assertEquals("c66c995e-571d-11eb-ae93-0242ac130002", caseId);
    Assertions.assertEquals("CANCEL", lastAction);
  }
}