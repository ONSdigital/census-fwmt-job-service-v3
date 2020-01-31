package uk.gov.ons.census.fwmt.jobservice.health;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.RABBIT_QUEUE_DOWN;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.RABBIT_QUEUE_UP;

@Component
public class RabbitQueuesHealthIndicator extends AbstractHealthIndicator {

  private final List<String> queues;
  private final ConnectionFactory connectionFactory;
  private final GatewayEventManager gatewayEventManager;

  private RabbitAdmin rabbitAdmin;

  public RabbitQueuesHealthIndicator(
      @Value("${rabbitmq.inboundQueue}") String inboundQueue,
      @Value("${rabbitmq.inboundDLQ}") String inboundDLQ,
      @Qualifier("connectionFactory") ConnectionFactory connectionFactory,
      GatewayEventManager gatewayEventManager) {
    this.queues = Arrays.asList(inboundQueue, inboundDLQ);
    this.connectionFactory = connectionFactory;
    this.gatewayEventManager = gatewayEventManager;
    this.rabbitAdmin = null;
  }

  private boolean checkQueue(String queueName) {
    Properties properties = rabbitAdmin.getQueueProperties(queueName);
    return (properties != null);
  }

  private Map<String, Boolean> getAccessibleQueues() {
    rabbitAdmin = new RabbitAdmin(connectionFactory);
    return queues.stream().collect(Collectors.toMap(queueName -> queueName, this::checkQueue));
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) {
    Map<String, Boolean> accessibleQueues = getAccessibleQueues();

    builder.withDetail("accessible-queues", accessibleQueues);

    if (accessibleQueues.containsValue(false)) {
      builder.down();
      gatewayEventManager.triggerErrorEvent(this.getClass(), "Cannot reach RabbitMQ", "<NA>", RABBIT_QUEUE_DOWN);
    } else {
      builder.up();
      gatewayEventManager.triggerEvent("<N/A>", RABBIT_QUEUE_UP);
    }
  }

}
