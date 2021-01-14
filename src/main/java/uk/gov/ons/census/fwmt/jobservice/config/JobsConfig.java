package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import uk.gov.ons.census.fwmt.jobservice.rabbit.QueueMigrator;

@Configuration
@EnableScheduling
public class JobsConfig {
  @Autowired
  private QueueMigrator queueMigrator;

  private String originQ = "GW.Transient.ErrorQ";
  private String destRoute = "GW.Field";

  @Scheduled(cron = "0 0/5 * * * *")
  public void migrateTransientQueueToGWFieldQueue() {
    queueMigrator.migrate(originQ, destRoute);
  }
}
