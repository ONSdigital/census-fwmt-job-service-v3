package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.config.RabbitMqConfig;

@Slf4j
@Component
public class ProcessRmFieldDlq {

  private RabbitTemplate rabbitTemplate;
  private AmqpAdmin amqpAdmin;
  private RabbitMqConfig config;

  public ProcessRmFieldDlq(@Qualifier("feedbackRabbitTemplate") RabbitTemplate rabbitTemplate, AmqpAdmin amqpAdmin, RabbitMqConfig config) {
    this.rabbitTemplate = rabbitTemplate;
    this.amqpAdmin = amqpAdmin;
    this.config = config;
  }

  public void processDlq() throws GatewayException {
    try {
      int messageCount = (int) amqpAdmin.getQueueProperties(config.inputDlq).get("QUEUE_MESSAGE_COUNT");

      for (int i = 0; i < messageCount; i++) {
        Message message = rabbitTemplate.receive(config.inputDlq);

        rabbitTemplate.send(config.inputQueue, message);
      }
    } catch (NullPointerException e) {
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST, "No messages in queue");
    }
  }
}
