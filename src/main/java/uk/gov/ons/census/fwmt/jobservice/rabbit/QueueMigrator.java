package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
public class QueueMigrator {

  private static final String QUEUE_MESSAGE_COUNT = "QUEUE_MESSAGE_COUNT";

  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate template;

  @Autowired
  @Qualifier("gatewayAmqpAdmin")
  private AmqpAdmin gatewayAmqpAdmin;

  public void migrate(String originQ, String destRoutingKey) {
    final Properties props = gatewayAmqpAdmin.getQueueProperties(originQ);
    if (props != null) {
      final Object cntValue = props.get(QUEUE_MESSAGE_COUNT);
      if (cntValue != null) {
        log.info("Migrating {} items from queue {} and redirecting to route {}", cntValue, originQ, destRoutingKey);
        int itemsToProcess = Integer.parseInt(cntValue.toString());
        while (itemsToProcess > 0) {
          final Message message = template.receive(originQ);
          if (message != null) {
            template.send(destRoutingKey, message);
          }
          itemsToProcess--;
        }

        log.info("Completed {} items from queue {} and redirecting to route {}", cntValue, originQ, destRoutingKey);
      } else {
        log.info("no items to migrate this time. ");
      }
    }
  }
}
