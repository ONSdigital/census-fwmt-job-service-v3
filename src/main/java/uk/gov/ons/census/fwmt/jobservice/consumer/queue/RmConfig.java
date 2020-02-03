package uk.gov.ons.census.fwmt.jobservice.consumer.queue;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.stereotype.Component;

@Component
public class RmConfig {

  private final String actionFieldQueueName;
  private final String actionFieldDLQName;
  private final AmqpAdmin amqpAdmin;

  public RmConfig(
      @Value("${rabbitmq.rmQueue}") String actionFieldQueueName,
      @Value("${rabbitmq.rmDeadLetter}") String actionFieldDLQName,
      AmqpAdmin amqpAdmin) {
    this.actionFieldQueueName = actionFieldQueueName;
    this.actionFieldDLQName = actionFieldDLQName;
    this.amqpAdmin = amqpAdmin;
  }

  @Bean(name = "RM")
  public Queue queue() {
    Queue queue = QueueBuilder.durable(actionFieldQueueName)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", actionFieldDLQName)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  @Bean(name = "RM.DLQ")
  public Queue deadLetterQueue() {
    Queue queue = QueueBuilder.durable(actionFieldDLQName).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  @Bean(name = "RM")
  public MessageListenerAdapter listenerAdapter(RmReceiver receiver) {
    return new MessageListenerAdapter(receiver, "receiveMessage");
  }

  @Bean(name = "RM")
  public SimpleMessageListenerContainer container(
      @Qualifier("RM") ConnectionFactory connectionFactory,
      @Qualifier("RM") MessageListenerAdapter messageListenerAdapter,
      RetryOperationsInterceptor retryOperationsInterceptor) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    Advice[] adviceChain = {retryOperationsInterceptor};
    container.setAdviceChain(adviceChain);
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(actionFieldQueueName);
    container.setMessageListener(messageListenerAdapter);
    return container;
  }

}
