package uk.gov.ons.census.fwmt.jobservice.rabbit;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;

import java.util.Date;

@Service
public class RmFieldRepublishProducer {

  @Autowired
  @Qualifier("republishRabbitTemplate")
  private RabbitTemplate rabbitTemplate;

  public void republish(FwmtActionInstruction fieldworkFollowup) {
    rabbitTemplate.convertAndSend("RM.Field", fieldworkFollowup, m -> {
      m.getMessageProperties().setHeader("__TypeId__", "uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction");
      m.getMessageProperties().setTimestamp(new Date());
      return m; } );
  }

  public void republish(FwmtCancelActionInstruction fieldworkFollowup) {
    rabbitTemplate.convertAndSend("RM.Field", fieldworkFollowup, m -> {
      m.getMessageProperties().setHeader("__TypeId__", "uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction");
      m.getMessageProperties().setTimestamp(new Date());
      return m; } );
  }
}
