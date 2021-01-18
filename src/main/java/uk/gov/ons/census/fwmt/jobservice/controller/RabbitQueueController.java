package uk.gov.ons.census.fwmt.jobservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.ons.census.fwmt.jobservice.rabbit.QueueMigrator;

@Slf4j
@RestController
@RequestMapping("/jobs")
public class RabbitQueueController {

  public static final String originQ = "GW.Transient.ErrorQ";
  public static final String destRoute = "GW.Field";

  @Autowired
  private QueueMigrator queueMigrator;

  @GetMapping(value = "/migratetransients")
  public String transferTransientMessagesToGWFieldQueue() {
    log.info("Executing migration from {} , to {}", originQ, destRoute);
    queueMigrator.migrate(originQ, destRoute);
    return "MIGRATION COMPLETE.";
  }
}
