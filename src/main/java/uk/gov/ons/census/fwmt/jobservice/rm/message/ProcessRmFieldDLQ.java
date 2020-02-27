package uk.gov.ons.census.fwmt.jobservice.rm.message;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.error.GatewayException;

@Slf4j
@Component
public class ProcessRmFieldDLQ {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Value("${rabbitmq.queues.rm.input}")
  private String RM_Q;

  @Autowired
  @Value("${rabbitmq.queues.rm.dlq}")
  private String RM_DLQ;


  public void processDLQ() throws GatewayException {
    int messageCount;
    Message message;

    try {
      messageCount = (int) amqpAdmin.getQueueProperties(RM_DLQ).get("QUEUE_MESSAGE_COUNT");

      for (int i = 0; i < messageCount; i++) {
        message = rabbitTemplate.receive(RM_DLQ);

        rabbitTemplate.send(RM_Q, message);
      }
    } catch (NullPointerException e) {
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, "No messages in queue");
    }
  }
}
