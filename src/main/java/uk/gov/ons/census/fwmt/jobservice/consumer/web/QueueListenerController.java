package uk.gov.ons.census.fwmt.jobservice.consumer.web;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.consumer.message.ProcessGatewayActionsDlq;
import uk.gov.ons.census.fwmt.jobservice.consumer.message.ProcessRmFieldDlq;

@Controller
public class QueueListenerController {

  // job service v3 only
  private final ProcessGatewayActionsDlq processGatewayActionsDLQ;

  // rm adapter only
  private final ProcessRmFieldDlq processRMFieldDLQ;

  private final SimpleMessageListenerContainer simpleMessageListenerContainer;

  public QueueListenerController(
      ProcessGatewayActionsDlq processGatewayActionsDLQ,
      ProcessRmFieldDlq processRMFieldDLQ,
      SimpleMessageListenerContainer simpleMessageListenerContainer) {
    this.processGatewayActionsDLQ = processGatewayActionsDLQ;
    this.processRMFieldDLQ = processRMFieldDLQ;
    this.simpleMessageListenerContainer = simpleMessageListenerContainer;
  }

  @GetMapping("/processDLQ")
  public ResponseEntity<String> startDLQProcessor() throws GatewayException {
    // job service v3 version
    processGatewayActionsDLQ.processDLQ();
    // rm adapter version
    processRMFieldDLQ.processDLQ();
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
