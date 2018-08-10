package uk.gov.ons.fwmt.job_service_v2.rmproducer;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import uk.gov.ons.fwmt.fwmtgatewaycommon.DummyTMResponse;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ons.fwmt.job_service_v2.ApplicationConfig.RM_ADAPTER_QUEUE;

@RunWith(MockitoJUnitRunner.class)
public class RMProducerTest {

  @InjectMocks RMProducer rmProducer;
  @Mock Queue queue;
  @Mock RabbitTemplate template;

  @Test
  public void send() {
    //Given
    DummyTMResponse dummyTMResponse = new DummyTMResponse();
    doNothing().when(template).convertAndSend(eq(RM_ADAPTER_QUEUE), eq(dummyTMResponse));

    //When
    rmProducer.send(dummyTMResponse);

    //Then
    verify(template).convertAndSend(eq(RM_ADAPTER_QUEUE), eq(dummyTMResponse));
  }
}