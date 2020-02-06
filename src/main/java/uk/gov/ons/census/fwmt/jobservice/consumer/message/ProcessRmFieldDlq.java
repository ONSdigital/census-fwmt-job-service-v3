package uk.gov.ons.census.fwmt.jobservice.consumer.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

@Slf4j
//@Component
public class ProcessRmFieldDlq {

  private final RabbitTemplate rabbitTemplate;
  private final AmqpAdmin amqpAdmin;
  private final ActionFieldQueueConfig actionFieldQueueConfig;

  public ProcessRmFieldDlq(
      RabbitTemplate rabbitTemplate,
      AmqpAdmin amqpAdmin,
      ActionFieldQueueConfig actionFieldQueueConfig) {
    this.rabbitTemplate = rabbitTemplate;
    this.amqpAdmin = amqpAdmin;
    this.actionFieldQueueConfig = actionFieldQueueConfig;
  }

  public void processDLQ() throws GatewayException {
    int messageCount;
    Message message;

    try {
      messageCount = (int) amqpAdmin.getQueueProperties(actionFieldQueueConfig.actionFieldDLQName)
          .get("QUEUE_MESSAGE_COUNT");

      for (int i = 0; i < messageCount; i++) {
        message = rabbitTemplate.receive(actionFieldQueueConfig.actionFieldDLQName);

        rabbitTemplate.send(actionFieldQueueConfig.actionFieldQueueName, message);
      }
    } catch (NullPointerException e) {
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, "No messages in queue");
    }
  }
}
