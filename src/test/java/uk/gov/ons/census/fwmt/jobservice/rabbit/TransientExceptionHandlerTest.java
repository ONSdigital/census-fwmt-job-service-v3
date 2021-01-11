package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.aspectj.bridge.MessageUtil.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransientExceptionHandlerTest {

  @InjectMocks
  private TransientExceptionHandler transientExceptionHandler;

  @Mock
  private RabbitTemplate rabbitTemplate;

  @DisplayName("Should Send message to TRANSIENT QUEUE via Routing Key - gw.transient.error")
  @Test
  void shouldPushMessageToTransientQueue() {

    final Message message = createMessage();
    transientExceptionHandler.handleMessage(message);
    verify(rabbitTemplate).convertAndSend(eq("GW.Error.Exchange"), eq("gw.transient.error"), eq(message));
  }

  @DisplayName("Should set the fail count to Zero if first time messages has been handled ")
  @Test
  void shouldSetFailCount() {
    fail("Not implemented yet");
  }

  @DisplayName("Should increment the fail count if message has been rejected before ")
  @Test
  void shouldIncrementFailCount() {
    fail("Not implemented yet");
  }

  @DisplayName("Should send failed message to perm queue if it has been processed more times than the maximum")
  @Test
  void shouldSendMessaageToPerm() {
    fail("Not implemented yet");
  }


  private Message createMessage() {
    final MessageProperties messageProperties = createQueueProperties();
    final Message message = new Message("dummydata".getBytes(), messageProperties);
    return message;
  }

  private MessageProperties createQueueProperties() {
    MessageProperties queueProperties = new MessageProperties();
    return queueProperties;
  }
}