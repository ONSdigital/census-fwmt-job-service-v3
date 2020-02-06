package uk.gov.ons.census.fwmt.jobservice.consumer.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

import static uk.gov.ons.census.fwmt.jobservice.consumer.message.GatewayActionsQueueConfig.GATEWAY_ACTIONS_DLQ;
import static uk.gov.ons.census.fwmt.jobservice.consumer.message.GatewayActionsQueueConfig.GATEWAY_ACTIONS_QUEUE;

@Slf4j
//@Component
public class ProcessGatewayActionsDlq {

  private final RabbitTemplate rabbitTemplate;
  private final AmqpAdmin amqpAdmin;

  public ProcessGatewayActionsDlq(
      @Autowired RabbitTemplate rabbitTemplate,
      @Autowired AmqpAdmin amqpAdmin) {
    this.rabbitTemplate = rabbitTemplate;
    this.amqpAdmin = amqpAdmin;
  }

  public void processDLQ() throws GatewayException {
    int messageCount;
    Message message;

    try {
      messageCount = (int) amqpAdmin.getQueueProperties(GATEWAY_ACTIONS_DLQ).get("QUEUE_MESSAGE_COUNT");

      for (int i = 0; i < messageCount; i++) {
        message = rabbitTemplate.receive(GATEWAY_ACTIONS_DLQ);

        rabbitTemplate.send(GATEWAY_ACTIONS_QUEUE, message);
      }
    } catch (NullPointerException e) {
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, "No messages in queue");
    }
  }
}
