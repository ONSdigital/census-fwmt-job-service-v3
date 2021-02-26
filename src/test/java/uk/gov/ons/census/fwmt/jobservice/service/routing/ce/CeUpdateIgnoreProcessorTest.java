package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

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
import uk.gov.ons.census.fwmt.jobservice.ce.CeRequestBuilder;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
import uk.gov.ons.census.fwmt.jobservice.service.routing.ignore.CeUpdateIgnoreProcessor;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.IGNORED_UPDATE;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CeUpdateIgnoreProcessorTest {

  @InjectMocks
  private CeUpdateIgnoreProcessor ceUpdateIgnoreProcessor;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Mock
  private GatewayCacheService cacheService;

  @Captor
  private ArgumentCaptor<String> spiedEvent;

  private GatewayCache createGatewayCache(String caseId, int type, int usualResidents) {
    return GatewayCache.builder().caseId(caseId).type(type).usualResidents(usualResidents).build();
  }

  @Test
  @DisplayName("Should log CE Update and ignore it")
  public void shouldLogCCeUpdateAndIgnoreIt() throws GatewayException {
    final FwmtActionInstruction instruction = CeRequestBuilder.ceUpdateInstruction();
    ceUpdateIgnoreProcessor.process(instruction);
    verify(eventManager).triggerEvent(any(), spiedEvent.capture(), any());
    String checkEvent = spiedEvent.getValue();
    Assertions.assertEquals(IGNORED_UPDATE, checkEvent);
  }
}