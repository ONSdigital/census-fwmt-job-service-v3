package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class GwRabbitConfig {
  
  @Bean
  @Qualifier("GW_errorE")
  public DirectExchange gwErrorExchange() {
    DirectExchange directExchange = new DirectExchange("GW.Error.Exchange");
    return directExchange;
  }

  @Bean
  @Qualifier("GW_errorQ")
  public Queue gwErrorQ() {
    Queue queue = QueueBuilder.durable("GW.ErrorQ")
        .withArgument("GW.Error.Exchange", "")
        .withArgument("gw.receiver.error", "GW.ErrorQ")
        .build();
    return queue;
  }
}
