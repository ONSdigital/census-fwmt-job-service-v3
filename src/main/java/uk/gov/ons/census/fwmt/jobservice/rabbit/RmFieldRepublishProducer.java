package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

@Service
public class RmFieldRepublishProducer {

  @Autowired
  @Qualifier("republishRabbitTemplate")
  private RabbitTemplate rabbitTemplate;

  public void republish(FwmtActionInstruction fieldworkFollowup) {
    rabbitTemplate.convertAndSend("RM.Field", fieldworkFollowup);
  }
}
