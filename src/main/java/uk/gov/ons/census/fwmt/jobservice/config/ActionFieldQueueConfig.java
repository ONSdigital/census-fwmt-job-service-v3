package uk.gov.ons.census.fwmt.jobservice.config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import uk.gov.ons.census.fwmt.jobservice.message.ActionInstructionReceiver;

@Configuration
public class ActionFieldQueueConfig {

  public String actionFieldQueueName;
  public String actionFieldDLQName;

  public ActionFieldQueueConfig(
      @Value("${rabbitmq.rmQueue}") String actionFieldQueueName,
      @Value("${rabbitmq.rmDeadLetter}") String actionFieldDLQName) {
    this.actionFieldQueueName = actionFieldQueueName;
    this.actionFieldDLQName = actionFieldDLQName;
  }

  @Autowired
  private AmqpAdmin amqpAdmin;

  //Queues
  @Bean
  public Queue actionFieldQueue() {
    Queue queue = QueueBuilder.durable(actionFieldQueueName)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", actionFieldDLQName)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  //Dead Letter Queue
  @Bean
  public Queue actionFieldDeadLetterQueue() {
    Queue queue = QueueBuilder.durable(actionFieldDLQName).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  //Listener Adapter
  @Bean
  public MessageListenerAdapter actionFieldListenerAdapter(ActionInstructionReceiver receiver) {
    return new MessageListenerAdapter(receiver, "receiveMessage");
  }

  //Message Listener
  @Bean
  public SimpleMessageListenerContainer actionFieldMessagerListener(
      @Qualifier("connectionFactory") ConnectionFactory connectionFactory,
      @Qualifier("actionFieldListenerAdapter") MessageListenerAdapter messageListenerAdapter,
      @Qualifier("interceptor") RetryOperationsInterceptor retryOperationsInterceptor) {
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    Advice[] adviceChain = {retryOperationsInterceptor};
    container.setAdviceChain(adviceChain);
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(actionFieldQueueName);
    container.setMessageListener(messageListenerAdapter);
    return container;
  }

}
