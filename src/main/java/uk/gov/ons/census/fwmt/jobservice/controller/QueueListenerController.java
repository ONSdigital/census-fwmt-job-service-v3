package uk.gov.ons.census.fwmt.jobservice.controller;

import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.rm.message.ProcessRmFieldDLQ;

//@Controller
public class QueueListenerController {

  @Autowired
  private ProcessRmFieldDLQ processRMFieldDLQ;

  @Autowired
  private SimpleMessageListenerContainer simpleMessageListenerContainer;

  @GetMapping("/processDLQ")
  public ResponseEntity<String> startDLQProcessor() throws GatewayException {
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
