package uk.gov.ons.census.fwmt.jobservice.health;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.TM_SERVICE_DOWN;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.TM_SERVICE_UP;

@Slf4j
@Component
public class TMHealthIndicator extends AbstractHealthIndicator {

  @Autowired
  private GatewayEventManager gatewayEventManager;

  @Value("${totalmobile.baseUrl}")
  private String tmBaseUrl;

  @Value("${totalmobile.healthcheckPath}")
  private String healthcheckPath;

  @Value("${totalmobile.username}")
  private String tmUsername;

  @Value("${totalmobile.password}")
  private String tmPassword;

  @Override protected void doHealthCheck(Health.Builder builder) {
    String swaggerUrl = tmBaseUrl + healthcheckPath;

    int responseCode = 0;

    try {
      responseCode = getResponseFromTM(swaggerUrl);

      if (responseCode == 200 || responseCode == 201 || responseCode == 202) {
        builder.up().withDetail(Integer.toString(responseCode), String.class);
        gatewayEventManager.triggerEvent("<N/A>", TM_SERVICE_UP, "response code", Integer.toString(responseCode));
        return;
      } else {
        gatewayEventManager
            .triggerErrorEvent(this.getClass(), null, "Cannot reach TM", "<NA>", TM_SERVICE_DOWN, "url", swaggerUrl,
                "Response Code", Integer.toString(responseCode));
      }

    } catch (Exception e) {
      builder.down().withDetail(e.getMessage(), e.getClass());
      gatewayEventManager
          .triggerErrorEvent(this.getClass(), e, "Cannot reach TM", "<NA>", TM_SERVICE_DOWN, "url", swaggerUrl);
    }
  }

  private int getResponseFromTM(String swaggerUrl) {
    int response = 0;

    RestTemplate restTemplate = new RestTemplate();

    ResponseEntity<Void> tmResponse = restTemplate.exchange(swaggerUrl, HttpMethod.GET, null, Void.class);
    response = tmResponse.getStatusCode().value();
    return response;
  }
}
