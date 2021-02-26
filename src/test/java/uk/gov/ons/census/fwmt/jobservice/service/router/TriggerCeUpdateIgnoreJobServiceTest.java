//package uk.gov.ons.census.fwmt.jobservice.service.router;
//
//import org.junit.jupiter.api.Assertions;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.ArgumentCaptor;
//import org.mockito.Captor;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.beans.factory.annotation.Autowired;
//import uk.gov.ons.census.fwmt.common.error.GatewayException;
//import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
//import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
//import uk.gov.ons.census.fwmt.jobservice.ce.CeRequestBuilder;
//import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
//import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
//import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
//import uk.gov.ons.census.fwmt.jobservice.service.JobService;
//import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
//import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
//import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
//import uk.gov.ons.census.fwmt.jobservice.service.routing.ignore.CeUpdateIgnoreProcessor;
//
//import java.time.Instant;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.verify;
//import static org.mockito.Mockito.when;
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.IGNORED_UPDATE;
//
//@ExtendWith(MockitoExtension.class)
//public class TriggerCeUpdateIgnoreJobServiceTest {
//
//  @InjectMocks
//  private JobService jobService;
//
//  @Mock
//  private CeUpdateIgnoreProcessor ceUpdateIgnoreProcessor;
//
//  @Mock
//  private CometRestClient cometRestClient;
//
//  @Mock
//  private GatewayEventManager eventManager;
//
//  @Mock
//  private RoutingValidator routingValidator;
//
//  @Mock
//  private GatewayCacheService cacheService;
//
//  @Autowired
//  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> updateProcessorMap;
//
//  @Captor
//  private ArgumentCaptor<String> spiedEvent;
//
//  private GatewayCache createGatewayCache(String caseId, int type, int usualResidents) {
//    return GatewayCache.builder().caseId(caseId).type(type).usualResidents(usualResidents).build();
//  }
//
//  @Test
//  @DisplayName("Should log CE Update and ignore it")
//  public void shouldLogCCeUpdateAndIgnoreIt() throws GatewayException {
//    final FwmtActionInstruction instruction = CeRequestBuilder.ceUpdateInstruction();
//    createGatewayCache(instruction.getCaseId(), 0, 0);
//    ProcessorKey key = ProcessorKey.buildKey(instruction);
//    when(updateProcessorMap.get(key)).thenReturn(new ArrayList<>());
//    jobService.processUpdate(instruction, Instant.now());
//    ceUpdateIgnoreProcessor.process(instruction);
//    verify(eventManager).triggerEvent(any(), spiedEvent.capture(), any());
//    String checkEvent = spiedEvent.getValue();
//    Assertions.assertEquals(IGNORED_UPDATE, checkEvent);
//  }
//}