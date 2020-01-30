package uk.gov.ons.census.fwmt.jobservice.message;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.config.ActionFieldQueueConfig;
import uk.gov.ons.census.fwmt.rmadapter.config.ActionFieldQueueConfig;

@Slf4j
@Component
public class ProcessRMFieldDLQ {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Autowired
  private ActionFieldQueueConfig actionFieldQueueConfig;

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
