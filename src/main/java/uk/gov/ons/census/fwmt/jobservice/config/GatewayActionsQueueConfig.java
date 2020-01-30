package uk.gov.ons.census.fwmt.jobservice.config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import uk.gov.ons.census.fwmt.jobservice.message.GatewayActionsReceiver;

@Configuration
public class GatewayActionsQueueConfig {
  public static final String GATEWAY_ACTIONS_QUEUE = "Gateway.Actions";
  public static final String GATEWAY_ACTIONS_EXCHANGE = "Gateway.Actions.Exchange";
  public static final String GATEWAY_ACTIONS_ROUTING_KEY = "Gateway.Action.Request";
  public static final String GATEWAY_ACTIONS_DLQ = "Gateway.ActionsDLQ";

  @Autowired
  private AmqpAdmin amqpAdmin;

  private int concurrentConsumers;

  public GatewayActionsQueueConfig(@Value("${rabbitmq.concurrentConsumers}") Integer concurrentConsumers) {
    this.concurrentConsumers = concurrentConsumers;
  }

  // Queue
  @Bean
  public Queue gatewayActionsQueue() {
    Queue queue = QueueBuilder.durable(GATEWAY_ACTIONS_QUEUE)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", GATEWAY_ACTIONS_DLQ)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  // Dead Letter Queue
  @Bean
  public Queue gatewayActionsDeadLetterQueue() {
    Queue queue = QueueBuilder.durable(GATEWAY_ACTIONS_DLQ).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  // Listener Adapter
  @Bean
  public MessageListenerAdapter gatewayActionsListenerAdapter(GatewayActionsReceiver receiver) {
    return new MessageListenerAdapter(receiver, "receiveMessage");
  }

  // Message Listener
  @Bean
  public SimpleMessageListenerContainer gatewayActionsMessageListener(
      @Qualifier("connectionFactory") ConnectionFactory connectionFactory,
      @Qualifier("gatewayActionsListenerAdapter") MessageListenerAdapter messageListenerAdapter,
      @Qualifier("interceptor") RetryOperationsInterceptor retryOperationsInterceptor) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    Advice[] adviceChain = {retryOperationsInterceptor};
    messageListenerAdapter.setMessageConverter(new Jackson2JsonMessageConverter());
    container.setAdviceChain(adviceChain);
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(GATEWAY_ACTIONS_QUEUE);
    container.setMessageListener(messageListenerAdapter);
    container.setConcurrentConsumers(concurrentConsumers);
    return container;
  }

  // Exchange
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
