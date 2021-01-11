package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class TransientExceptionHandler {
  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate gatewayRabbitTemplate;

  public void handleMessage(Message message) {
    gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.transient.error", message);
  }
}
