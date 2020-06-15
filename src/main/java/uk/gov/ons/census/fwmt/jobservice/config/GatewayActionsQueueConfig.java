package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayActionsQueueConfig {

  public static final String GATEWAY_ACTIONS_QUEUE = "Gateway.Actions";
  public static final String GATEWAY_ACTIONS_EXCHANGE = "Gateway.Actions.Exchange";
  public static final String GATEWAY_ACTIONS_ROUTING_KEY = "Gateway.Action.Request";
  public static final String GATEWAY_ACTIONS_DLQ = "Gateway.ActionsDLQ";

  @Autowired
  private AmqpAdmin amqpAdmin;

  //Queues
  @Bean
  public Queue gatewayActionsQueue() {
    Queue queue = QueueBuilder.durable(GATEWAY_ACTIONS_QUEUE)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", GATEWAY_ACTIONS_DLQ)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  //Dead Letter Queue
  @Bean
  public Queue gatewayActionsDeadLetterQueue() {
    Queue queue = QueueBuilder.durable(GATEWAY_ACTIONS_DLQ).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  //Exchange
  @Bean
  public DirectExchange gatewayActionsExchange() {
    DirectExchange directExchange = new DirectExchange(GATEWAY_ACTIONS_EXCHANGE);
    directExchange.setAdminsThatShouldDeclare(amqpAdmin);
    return directExchange;
  }

  // Bindings
  @Bean
  public Binding gatewayActionsBinding(@Qualifier("gatewayActionsQueue") Queue gatewayActionsQueue,
      @Qualifier("gatewayActionsExchange") DirectExchange gatewayActionsExchange) {
    Binding binding = BindingBuilder.bind(gatewayActionsQueue).to(gatewayActionsExchange)
        .with(GATEWAY_ACTIONS_ROUTING_KEY);
    binding.setAdminsThatShouldDeclare(amqpAdmin);
    return binding;
  }
}
