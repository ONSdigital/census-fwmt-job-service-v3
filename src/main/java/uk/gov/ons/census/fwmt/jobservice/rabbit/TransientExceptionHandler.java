package uk.gov.ons.census.fwmt.jobservice.rabbit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
public class TransientExceptionHandler {
  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate gatewayRabbitTemplate;

  public static final int MAX_RETRY_COUNT = 3;

  public void handleMessage(Message message) {
    Integer retryCount = message.getMessageProperties().getHeader("retryCount");
    retryCount = (null== retryCount) ? retryCount=1 : ++retryCount;
    message.getMessageProperties().setHeader("retryCount",retryCount);
    if(retryCount >MAX_RETRY_COUNT){
      log.error("Retry limit exceeded and message has been routed to permanent queue");
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.permanent.error", message);
    }else {
      gatewayRabbitTemplate.convertAndSend("GW.Error.Exchange", "gw.transient.error", message);
    }
  }
}
