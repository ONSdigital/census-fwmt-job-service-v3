package uk.gov.ons.census.fwmt.jobservice.config;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultClassMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.support.RetryTemplate;
import uk.gov.ons.census.fwmt.common.retry.DefaultListenerSupport;
import uk.gov.ons.census.fwmt.common.retry.GatewayMessageRecover;
import uk.gov.ons.census.fwmt.common.retry.GatewayRetryPolicy;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {
  public final String inputQueue;
  public final String inputDlq;
  private final String username;
  private final String password;
  private final String hostname;
  private final int port;
  private final String virtualHost;
  private final int initialInterval;
  private final double multiplier;
  private final int maxInterval;
  private final int maxRetries;
  private final int prefetchCount;

  public RabbitMqConfig(
      @Value("${app.rabbitmq.rm.username}") String username,
      @Value("${app.rabbitmq.rm.password}") String password,
      @Value("${app.rabbitmq.rm.host}") String hostname,
      @Value("${app.rabbitmq.rm.port}") int port,
      @Value("${app.rabbitmq.rm.virtualHost}") String virtualHost,
      @Value("${app.rabbitmq.rm.initialInterval}") int initialInterval,
      @Value("${app.rabbitmq.rm.multiplier}") double multiplier,
      @Value("${app.rabbitmq.rm.maxInterval}") int maxInterval,
      @Value("${app.rabbitmq.rm.maxRetries:1}") int maxRetries,
      @Value("${app.rabbitmq.rm.prefetchCount}") int prefetchCount,
      @Value("${app.rabbitmq.rm.queues.rm.input}") String inputQueue,
      @Value("${app.rabbitmq.rm.queues.rm.dlq}") String inputDlq) {
    this.username = username;
    this.password = password;
    this.hostname = hostname;
    this.port = port;
    this.virtualHost = virtualHost;
    this.initialInterval = initialInterval;
    this.multiplier = multiplier;
    this.maxInterval = maxInterval;
    this.maxRetries = maxRetries;
    this.inputQueue = inputQueue;
    this.inputDlq = inputDlq;
    this.prefetchCount = prefetchCount;
  }

  @Bean("rmRabbitTemplate")
  public RabbitTemplate rabbitTemplate(@Qualifier("rmConnectionFactory") ConnectionFactory gatewayConnectionFactory, @Qualifier("JS") MessageConverter messageConverter) {
    RabbitTemplate template = new RabbitTemplate(gatewayConnectionFactory);
    template.setMessageConverter(messageConverter);
    return template;
  }

  @Bean("rmConnectionFactory")
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

  @Bean
  @Qualifier("JS")
  public MessageConverter jsonMessageConverter() {
    Jackson2JsonMessageConverter jsonMessageConverter = new Jackson2JsonMessageConverter();
    jsonMessageConverter.setClassMapper(classMapper());
    return jsonMessageConverter;
  }

  @Bean
  @Qualifier("JS")
  public DefaultClassMapper classMapper() {
    DefaultClassMapper classMapper = new DefaultClassMapper();
    Map<String, Class<?>> idClassMapping = new HashMap<>();
    idClassMapping.put("uk.gov.ons.census.fwmtadapter.model.dto.fwmt.FwmtActionInstruction", FwmtActionInstruction.class);
    idClassMapping.put("uk.gov.ons.census.fwmtadapter.model.dto.fwmt.FwmtCancelActionInstruction", FwmtCancelActionInstruction.class);
    idClassMapping.put("uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction", FwmtActionInstruction.class);
    idClassMapping.put("uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction", FwmtCancelActionInstruction.class);
    classMapper.setIdClassMapping(idClassMapping);
    classMapper.setTrustedPackages("*");
    return classMapper;
  }

  @Bean
  public RetryOperationsInterceptor interceptor() {
    RetryOperationsInterceptor interceptor = new RetryOperationsInterceptor();
    interceptor.setRecoverer(new GatewayMessageRecover());
    interceptor.setRetryOperations(retryTemplate());
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

    GatewayRetryPolicy gatewayRetryPolicy = new GatewayRetryPolicy(maxRetries);
    retryTemplate.setRetryPolicy(gatewayRetryPolicy);

    retryTemplate.registerListener(new DefaultListenerSupport());

    return retryTemplate;
  }

  @Bean
  public Queue queue() {
    Queue queue = QueueBuilder.durable(inputQueue)
        .withArgument("x-dead-letter-exchange", "")
        .withArgument("x-dead-letter-routing-key", inputDlq)
        .build();
    queue.setAdminsThatShouldDeclare(amqpAdmin());
    return queue;
  }

  @Bean
  public Queue deadLetterQueue() {
    Queue queue = QueueBuilder.durable(inputDlq).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin());
    return queue;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory retryContainerFactory(
      @Qualifier("rmConnectionFactory") ConnectionFactory connectionFactory, RetryOperationsInterceptor interceptor,
      @Qualifier("JS") MessageConverter jsonMessageConverter) {
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
    factory.setConnectionFactory(connectionFactory);
    factory.setMessageConverter(jsonMessageConverter);
    Advice[] adviceChain = {interceptor};
    factory.setAdviceChain(adviceChain);

    return factory;
  }

}