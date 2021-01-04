package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class CeSwitchCreateProcessorTest {

  @InjectMocks
  private CeSwitchCreateProcessor ceSwitchCreateProcessor;

  @Mock
  private CometRestClient cometRestClient;

  @Mock
  private GatewayEventManager eventManager;

  @Mock
  private RoutingValidator routingValidator;

  @Mock
  private GatewayCacheService cacheService;

  private FwmtActionInstruction createInstruction() {
    return FwmtActionInstruction.builder().caseRef("345").build();
  }

  private GatewayCache createGatewayCache() {
    return GatewayCache.builder().caseId("").build();
  }

  // what are we going to test here -
  @Test
  public void shouldTriggerErrorEvent() throws GatewayException {

    FwmtActionInstruction instruction = createInstruction();
    ceSwitchCreateProcessor.process(instruction, createGatewayCache(), Instant.now());

    assertTrue(false);
  }

  // put in all the outcomes - switch - what comet requests are being made
  @Test
  public void shouldMessagEcomment() {
    // put in all the outcomes - switch - what comet requests are being made

    // what things are happening in that switch ?
  }
}