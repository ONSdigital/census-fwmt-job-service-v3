package uk.gov.ons.census.fwmt.jobservice.health;

import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayActionsQueueConfig;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.RABBIT_QUEUE_DOWN;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.RABBIT_QUEUE_UP;

@Component
public class RabbitQueuesHealthIndicator extends AbstractHealthIndicator {

  private List<String> queues;

  @Autowired
  @Qualifier("connectionFactory")
  private ConnectionFactory connectionFactory;

  @Autowired
  private GatewayEventManager gatewayEventManager;

  private RabbitAdmin rabbitAdmin;

  public RabbitQueuesHealthIndicator(
      @Value("${rabbitmq.inboundQueue}") String inboundQueue,
      @Value("${rabbitmq.inboundDLQ}") String inboundDLQ) {
    queues = Arrays.asList(inboundQueue, inboundDLQ);
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
