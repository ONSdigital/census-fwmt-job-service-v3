package uk.gov.ons.census.fwmt.jobservice.rm;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.RetryOperations;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;

import uk.gov.ons.census.fwmt.common.retry.DefaultListenerSupport;
import uk.gov.ons.census.fwmt.common.retry.GatewayMessageRecover;
import uk.gov.ons.census.fwmt.common.retry.GatewayRetryPolicy;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

@Configuration
public class RabbitMqConfig {

  private final String username;
  private final String password;
  private final String hostname;
  private final int port;
  private final String virtualHost;
  private final int initialInterval;
  private final double multiplier;
  private final int maxInterval;

  public final String inputQueue;
  public final String inputDlq;

  public RabbitMqConfig(
      @Value("${rabbitmq.username}") String username,
      @Value("${rabbitmq.password}") String password,
      @Value("${rabbitmq.hostname}") String hostname,
      @Value("${rabbitmq.port}") int port,
      @Value("${rabbitmq.virtualHost}") String virtualHost,
      @Value("${rabbitmq.initialInterval}") int initialInterval,
      @Value("${rabbitmq.multiplier}") double multiplier,
      @Value("${rabbitmq.maxInterval}") int maxInterval,
      @Value("${rabbitmq.queues.rm.input}") String inputQueue,
      @Value("${rabbitmq.queues.rm.dlq}") String inputDlq) {
    this.username = username;
    this.password = password;
    this.hostname = hostname;
    this.port = port;
    this.virtualHost = virtualHost;
    this.initialInterval = initialInterval;
    this.multiplier = multiplier;
    this.maxInterval = maxInterval;
    this.inputQueue = inputQueue;
    this.inputDlq = inputDlq;
  }

  @Bean
  public ConnectionFactory connectionFactory() {
    CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(hostname, port);

    cachingConnectionFactory.setVirtualHost(virtualHost);
    cachingConnectionFactory.setPassword(password);
    cachingConnectionFactory.setUsername(username);

    return cachingConnectionFactory;
  }

  @Bean
  public AmqpAdmin amqpAdmin() {
    return new RabbitAdmin(connectionFactory());
  }

  // TODO: It doesn't seem right that these qualifiers are appended with the kind of class they are. 'JS' would do, if they are about Javascript
  // TODO: It looks like this is used elsewhere. Should this be in a more general config?
  @Bean("JS_MC")
  public MessageConverter jsonMessageConverter(@Qualifier("JS_CM") DefaultClassMapper classMapper) {
    Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
    jsonMessageConverter.setClassMapper(classMapper);
    return jsonMessageConverter;
  }

  // TODO: It doesn't seem right that these qualifiers are appended with the kind of class they are. 'JS' would do, if they are about Javascript
  @Bean("JS_CM")
  public DefaultClassMapper classMapper() {
    DefaultClassMapper classMapper = new DefaultClassMapper();
    Map<String, Class<?>> idClassMapping = new HashMap<>();
    idClassMapping.put("uk.gov.ons.census.fwmt.jobservice.rm.dto.FieldworkFollowup", FieldworkFollowup.class);
    classMapper.setIdClassMapping(idClassMapping);
    classMapper.setTrustedPackages("*");
    return classMapper;
  }

  @Bean
  public RetryOperationsInterceptor interceptor(@Qualifier("retryTemplate") RetryOperations retryOperations) {
    RetryOperationsInterceptor interceptor = new RetryOperationsInterceptor();
    interceptor.setRecoverer(new GatewayMessageRecover());
    interceptor.setRetryOperations(retryOperations);
    return interceptor;
  }

  @Bean
  public RetryTemplate retryTemplate() {
    RetryTemplate retryTemplate = new RetryTemplate();

    ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
    backOffPolicy.setInitialInterval(initialInterval);
    backOffPolicy.setMultiplier(multiplier);
    backOffPolicy.setMaxInterval(maxInterval);
    retryTemplate.setBackOffPolicy(backOffPolicy);

    GatewayRetryPolicy gatewayRetryPolicy = new GatewayRetryPolicy();
    retryTemplate.setRetryPolicy(gatewayRetryPolicy);

    retryTemplate.registerListener(new DefaultListenerSupport());

    return retryTemplate;
  }

  @Bean(name = "RM_Q")
  public Queue queue() {
    Queue queue = QueueBuilder.durable(inputQueue)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", inputDlq)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin());
    return queue;
  }

  @Bean(name = "RM_DLQ")
  public Queue deadLetterQueue() {
    Queue queue = QueueBuilder.durable(inputDlq).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin());
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
