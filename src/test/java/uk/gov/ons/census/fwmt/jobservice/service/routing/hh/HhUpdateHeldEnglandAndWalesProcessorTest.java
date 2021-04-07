package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.hh.HhRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class HhUpdateHeldEnglandAndWalesProcessorTest {

  @InjectMocks
  private HhUpdateHeldEnglandAndWales hhUpdateHeldEnglandAndWales;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayCache gatewayCache;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  private static final String HH_UPDATE_HELD = "HH_UPDATE_HELD";


  @Test
  @DisplayName("Should hold a HH update that does not exists in FWMT")
  public void shouldHoldAHhUpdateThatDoesNotExistInFwmt() throws GatewayException {
    final FwmtActionInstruction instruction = HhRequestBuilder.updateActionInstruction();
    GatewayCache gatewayCache = GatewayCache.builder()
        .caseId("ac623e62-4f4b-11eb-ae93-0242ac130002").existsInFwmt(false).build();
    hhUpdateHeldEnglandAndWales.process(instruction, gatewayCache,  Instant.now());
    verify(eventManager, atLeast(1)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(HH_UPDATE_HELD, checkEvent);
  }

  @Test
  @DisplayName("Should hold a HH update that does not exists in cache")
  public void shouldHoldAHhUpdateThatDoesNotExistInCache() throws GatewayException {
    final FwmtActionInstruction instruction = HhRequestBuilder.updateActionInstruction();
    hhUpdateHeldEnglandAndWales.process(instruction, null,  Instant.now());
    verify(eventManager, atLeast(1)).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(HH_UPDATE_HELD, checkEvent);
  }
}