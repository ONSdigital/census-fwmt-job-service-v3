package uk.gov.ons.census.fwmt.jobservice.rm;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.stereotype.Component;

@Component
public class RmConfig {

  @Autowired
  @Value("${rabbitmq.queues.rm.input}")
  private String inputQueue;

  @Autowired
  @Value("${rabbitmq.queues.rm.dlq}")
  private String inputDlq;

  @Autowired
  private AmqpAdmin amqpAdmin;

  @Bean(name = "RM_Q")
  public Queue queue() {
    Queue queue = QueueBuilder.durable(inputQueue)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", inputDlq)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  @Bean(name = "RM_DLQ")
  public Queue deadLetterQueue() {
    Queue queue = QueueBuilder.durable(inputDlq).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin);
    return queue;
  }

  // TODO: This doesn't need to be suffixed with a type
  @Bean(name = "RM_LA")
  public MessageListenerAdapter listenerAdapter(RmReceiver receiver) {
    return new MessageListenerAdapter(receiver, "receiveMessage");
  }

  // TODO: This doesn't need to be suffixed with a type
  @Bean(name = "RM_C")
  public SimpleMessageListenerContainer container(
      ConnectionFactory connectionFactory,
      @Qualifier("RM_LA") MessageListenerAdapter listenerAdapter,
      @Qualifier("JS_MC") MessageConverter jsonMessageConverter,
      RetryOperationsInterceptor retryOperationsInterceptor) {

    listenerAdapter.setMessageConverter(jsonMessageConverter);

    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
    Advice[] adviceChain = {retryOperationsInterceptor};
    container.setAdviceChain(adviceChain);
    container.setConnectionFactory(connectionFactory);
    container.setQueueNames(inputQueue);
    container.setMessageListener(listenerAdapter);

    return container;
  }

}
