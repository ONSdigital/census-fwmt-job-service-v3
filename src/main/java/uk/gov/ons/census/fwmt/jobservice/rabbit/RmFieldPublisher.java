package uk.gov.ons.census.fwmt.jobservice.rabbit;

import java.time.Instant;
import java.util.Date;

import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

@Service
public class RmFieldPublisher {

  @Autowired
 // @Qualifier("feedbackRabbitTemplate")
  private RabbitTemplate rabbitTemplate;

  public void publish(FwmtCancelActionInstruction cancelActionInstruction) {
    rabbitTemplate.convertAndSend("RM.Field", cancelActionInstruction, new MessagePostProcessor() {
      
      @Override
      public Message postProcessMessage(Message message) throws AmqpException {
        long epochMilli = Instant.now().toEpochMilli();
        message.getMessageProperties().setTimestamp(new Date(epochMilli));
        return message;
      }
    });
  }

  public void publish(FwmtActionInstruction actionInstruction) {
    rabbitTemplate.convertAndSend("RM.Field", actionInstruction, new MessagePostProcessor() {
      
      @Override
      public Message postProcessMessage(Message message) throws AmqpException {
        long epochMilli = Instant.now().toEpochMilli();
        message.getMessageProperties().setTimestamp(new Date(epochMilli));
        return message;
      }
    });
  }
}
