package uk.gov.ons.census.fwmt.jobservice.rm;

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
public class ProcessRmFieldDlq {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Value("${rabbitmq.queues.rm.input}")
  private String rmQ;

  @Autowired
  @Value("${rabbitmq.queues.rm.dlq}")
  private String rmDlq;


  public void processDlq() throws GatewayException {
    int messageCount;
    Message message;

    try {
      messageCount = (int) amqpAdmin.getQueueProperties(rmDlq).get("QUEUE_MESSAGE_COUNT");

      for (int i = 0; i < messageCount; i++) {
        message = rabbitTemplate.receive(rmDlq);

        rabbitTemplate.send(rmQ, message);
      }
    } catch (NullPointerException e) {
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, "No messages in queue");
    }
  }
}
