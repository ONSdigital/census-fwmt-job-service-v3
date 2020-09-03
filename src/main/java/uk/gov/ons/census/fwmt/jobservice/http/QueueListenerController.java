package uk.gov.ons.census.fwmt.jobservice.http;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.service.ProcessRmFieldDlq;

@Controller
public class QueueListenerController {

  @Autowired
  private ProcessRmFieldDlq processRMFieldDLQ;

  private SimpleMessageListenerContainer simpleMessageListenerContainer;

  @GetMapping("/processDLQ")
  public ResponseEntity<String> startDLQProcessor() throws GatewayException {
    processRMFieldDLQ.processDlq();
    return ResponseEntity.ok("DLQ listener started.");
  }

  @GetMapping("/startListener")
  public ResponseEntity<String> startListener() {
    simpleMessageListenerContainer.start();
    return ResponseEntity.ok("Queue listener started.");
  }

  @GetMapping("/stopListener")
  public ResponseEntity<String> stopListener() {
    simpleMessageListenerContainer.stop();
    return ResponseEntity.ok("Queue listener stopped.");
  }
}
