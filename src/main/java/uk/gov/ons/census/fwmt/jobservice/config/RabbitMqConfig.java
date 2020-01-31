package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.support.RetryTemplate;
import uk.gov.ons.census.fwmt.common.retry.DefaultListenerSupport;
import uk.gov.ons.census.fwmt.common.retry.GatewayRetryPolicy;

@Configuration
public class RabbitMqConfig {

  private final int initialInterval;
  private final double multiplier;
  private final int maxInterval;

  public RabbitMqConfig(
      @Value("${rabbitmq.initialinterval}") int initialInterval,
      @Value("${rabbitmq.multiplier}") double multiplier,
      @Value("${rabbitmq.maxInterval}") int maxInterval) {
    this.initialInterval = initialInterval;
    this.multiplier = multiplier;
    this.maxInterval = maxInterval;
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
}
