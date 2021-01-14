package uk.gov.ons.census.fwmt.jobservice.rabbit;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class TransientExceptionHandler {
  @Autowired
  @Qualifier("GW_EVENT_RT")
  private RabbitTemplate gatewayRabbitTemplate;

  @Value("${app.rabbitmq.gw.maxRetryCount}")
  private int maxRetryCount;
  @Value("${app.rabbitmq.gw.exchanges.error}")
  private String errorExchange;
  @Value("${app.rabbitmq.gw.routingkey.perm}")
  private String permanentRoutingKey;
  @Value("${app.rabbitmq.gw.routingkey.trans}")
  private String transientRoutingKey;


  @PostConstruct
  public void TransientExceptionHandler(){
   log.info("TransientExceptionHandler maxRetryCount :{}" , maxRetryCount);
   log.info("TransientExceptionHandler errorExchange :{}", errorExchange);
   log.info("TransientExceptionHandler permanent routing key :{}", permanentRoutingKey);
   log.info("TransientExceptionHandler transient routing key :{}", transientRoutingKey);
  }

  public void handleMessage(Message message) {
    Integer retryCount = message.getMessageProperties().getHeader("retryCount");
    if(null == retryCount){
      retryCount =1;
    }else {
      retryCount ++;
    }
    log.info("TransientExceptionHandler retryCount :{}" ,  retryCount);
    message.getMessageProperties().setHeader("retryCount",retryCount);

    if(retryCount >maxRetryCount){
      log.error("Retry limit exceeded and message has been routed to permanent queue");
      gatewayRabbitTemplate.convertAndSend(errorExchange, permanentRoutingKey, message);
    }else {
      gatewayRabbitTemplate.convertAndSend(errorExchange, transientRoutingKey, message);
    }
  }
}
