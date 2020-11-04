package uk.gov.ons.census.fwmt.jobservice.config;

import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
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
      @Value("${rabbitmq.username}") String username,
      @Value("${rabbitmq.password}") String password,
      @Value("${rabbitmq.hostname}") String hostname,
      @Value("${rabbitmq.port}") int port,
      @Value("${rabbitmq.virtualHost}") String virtualHost,
      @Value("${rabbitmq.initialInterval}") int initialInterval,
      @Value("${rabbitmq.multiplier}") double multiplier,
      @Value("${rabbitmq.maxInterval}") int maxInterval,
      @Value("${rabbitmq.maxRetries:1}") int maxRetries,
      @Value("${rabbitmq.prefetchCount}") int prefetchCount,
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
    this.maxRetries = maxRetries;
    this.inputQueue = inputQueue;
    this.inputDlq = inputDlq;
    this.prefetchCount = prefetchCount;
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
  public Queue deadLetterQueue() {
    Queue queue = QueueBuilder.durable(inputDlq).build();
    queue.setAdminsThatShouldDeclare(amqpAdmin());
    return queue;
  }

  @Bean
  public SimpleRabbitListenerContainerFactory retryContainerFactory(
    ConnectionFactory connectionFactory, RetryOperationsInterceptor interceptor,
    @Qualifier("JS") MessageConverter jsonMessageConverter) {
      SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
      factory.setConnectionFactory(connectionFactory);
      factory.setMessageConverter(jsonMessageConverter);
      Advice[] adviceChain = { interceptor };
      factory.setAdviceChain(adviceChain);
   
      return factory;
  }
}