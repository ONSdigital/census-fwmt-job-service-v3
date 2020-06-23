package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayActionsQueueConfig;

@Component
public class GatewayActionProducer {

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Autowired
  @Qualifier("gatewayActionsExchange")
  private DirectExchange gatewayActionsExchange;
  
  @Retryable
  public void sendMessage(FwmtActionInstruction gatewayMessage) {

    rabbitTemplate.convertAndSend(gatewayActionsExchange.getName(), GatewayActionsQueueConfig.RM_FIELD_ROUTING_KEY, gatewayMessage);
  }
}
