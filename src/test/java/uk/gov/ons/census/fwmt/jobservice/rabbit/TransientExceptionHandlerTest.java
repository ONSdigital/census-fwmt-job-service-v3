package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransientExceptionHandlerTest {

  @InjectMocks
  private TransientExceptionHandler transientExceptionHandler;

  @Captor
  private ArgumentCaptor<Message> messageArgumentCaptor;

  @Mock
  private RabbitTemplate rabbitTemplate;

  @BeforeEach
  public void setup(){
    ReflectionTestUtils.setField(transientExceptionHandler,"maxRetryCount",5);
    ReflectionTestUtils.setField(transientExceptionHandler,"errorExchange","GW.Error.Exchange");
    ReflectionTestUtils.setField(transientExceptionHandler,"permanentRoutingKey","gw.permanent.error");
    ReflectionTestUtils.setField(transientExceptionHandler,"transientRoutingKey","gw.transient.error");
  }

  @DisplayName("Should Send message to TRANSIENT QUEUE via Routing Key - gw.transient.error")
  @Test
  void shouldPushMessageToTransientQueue() {
    final Message message = createMessage(null);
    transientExceptionHandler.handleMessage(message);
    verify(rabbitTemplate).convertAndSend(eq("GW.Error.Exchange"), eq("gw.transient.error"), eq(message));
  }

  @DisplayName("Should set the retryCount to one if first time messages has been handled ")
  @Test
  void shouldSetFailCount() {
    final Message message = createMessage(null);
    transientExceptionHandler.handleMessage(message);
    verify(rabbitTemplate).convertAndSend(anyString(),anyString(),messageArgumentCaptor.capture());
    Message resultMessage = messageArgumentCaptor.getValue();
    int retryCount = resultMessage.getMessageProperties().getHeader("retryCount");
    Assertions.assertEquals(1,retryCount);
  }

  @DisplayName("Should increment the retryCount if message has been rejected before ")
  @Test
  void shouldIncrementFailCount() {
    final Message message = createMessage(1);
    transientExceptionHandler.handleMessage(message);
    verify(rabbitTemplate).convertAndSend(anyString(),anyString(),messageArgumentCaptor.capture());
    Message resultMessage = messageArgumentCaptor.getValue();
    int retryCount = resultMessage.getMessageProperties().getHeader("retryCount");
    Assertions.assertEquals(2,retryCount);
  }

  @DisplayName("Should send failed message to perm queue if it has been processed more times than the maximum")
  @Test
  void shouldSendMessaageToPerm() {
    final Message message = createMessage(5);
    transientExceptionHandler.handleMessage(message);
    verify(rabbitTemplate).convertAndSend(eq("GW.Error.Exchange"), eq("gw.permanent.error"), eq(message));
    verify(rabbitTemplate,never()).convertAndSend(eq("GW.Error.Exchange"), eq("gw.transient.error"), eq(message));
  }


  private Message createMessage(Integer retryCount) {
    final MessageProperties messageProperties = createQueueProperties();
    final Message message = new Message("dummydata".getBytes(), messageProperties);
    messageProperties.setHeader("retryCount",retryCount);
    return message;
  }

  private MessageProperties createQueueProperties() {
    MessageProperties queueProperties = new MessageProperties();
    return queueProperties;
  }
}