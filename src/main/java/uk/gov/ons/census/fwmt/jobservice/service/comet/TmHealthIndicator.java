package uk.gov.ons.census.fwmt.jobservice.service.comet;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.TM_SERVICE_DOWN;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.TM_SERVICE_UP;

@Slf4j
@Component
public class TmHealthIndicator extends AbstractHealthIndicator {

  private final GatewayEventManager gatewayEventManager;
  private final String swaggerUrl;
  private final RestTemplate restTemplate;

  public TmHealthIndicator(
      GatewayEventManager gatewayEventManager,
      @Value("${totalmobile.baseUrl}") String tmBaseUrl,
      @Value("${totalmobile.healthcheckPath}") String healthcheckPath,
      RestTemplateBuilder restTemplateBuilder
      //@Qualifier("TM Anon") RestTemplate restTemplate
  ) {
    this.gatewayEventManager = gatewayEventManager;
    this.swaggerUrl = tmBaseUrl + healthcheckPath;
    this.restTemplate = restTemplateBuilder.build();
  }

  @Bean(name = "TM Anon")
  public RestTemplate restTemplate() {
    return restTemplate;
  }

  @Override protected void doHealthCheck(Health.Builder builder) {
    try {
      HttpStatus responseCode = restTemplate.exchange(swaggerUrl, HttpMethod.GET, null, Void.class).getStatusCode();

      if (responseCode.is2xxSuccessful()) {
        builder.up().withDetail(responseCode.toString(), String.class).build();
        gatewayEventManager.triggerEvent("<N/A>", TM_SERVICE_UP, "response code", responseCode.toString());
      } else {
        builder.down().build();
        gatewayEventManager.triggerErrorEvent(this.getClass(), null, "Cannot reach TM", "<NA>",
            TM_SERVICE_DOWN, "url", swaggerUrl, "Response Code", responseCode.toString());
      }

    } catch (Exception e) {
      builder.down().withDetail(e.getMessage(), e.getClass()).build();
      gatewayEventManager.triggerErrorEvent(this.getClass(), e, "Cannot reach TM", "<NA>",
          TM_SERVICE_DOWN, "url", swaggerUrl);
    }
  }
}
