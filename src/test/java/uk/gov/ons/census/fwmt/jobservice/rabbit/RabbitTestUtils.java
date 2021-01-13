package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

public class RabbitTestUtils {
  static Message createMessage(String data, Integer retryCount) {
    final MessageProperties messageProperties = createQueueProperties();
    final Message message = new Message(data.getBytes(), messageProperties);
    messageProperties.setHeader("retryCount", retryCount);
    return message;
  }

  static Message createMessage(Integer retryCount) {
    final MessageProperties messageProperties = createQueueProperties();
    final Message message = new Message("dummydata".getBytes(), messageProperties);
    messageProperties.setHeader("retryCount", retryCount);
    return message;
  }

  static MessageProperties createQueueProperties() {
    MessageProperties queueProperties = new MessageProperties();
    return queueProperties;
  }
}
