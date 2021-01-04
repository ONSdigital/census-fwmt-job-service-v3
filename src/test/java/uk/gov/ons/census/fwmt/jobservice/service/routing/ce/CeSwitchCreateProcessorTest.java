package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import java.time.Instant;


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

  @Test
  @DisplayName("Should throw Gateway Exception and trigger event for invalid survey type")
  public void shouldHandleIncorrectSurveyTypeCE() {
    final FwmtActionInstruction instruction = createInstruction();
    instruction.setSurveyType(SurveyType.AC);
    instruction.setCaseId("1234");
    Assertions.assertThrows(GatewayException.class, () -> {
      ceSwitchCreateProcessor.process(instruction, createGatewayCache(), Instant.now());
    });
  }

  @Test
  @DisplayName("Should handle type - CE_EST_D ")
  public void shouldMessageForCE_EST_D() {

  }
}