package uk.gov.ons.census.fwmt.jobservice.service.comet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class TmConfig {

  private final String userName;
  private final String password;

  public TmConfig(
      @Value("${totalmobile.username}") String userName,
      @Value("${totalmobile.password}") String password) {
    this.userName = userName;
    this.password = password;
  }

  @Bean(name = "TM")
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.errorHandler(new CometRestClientResponseErrorHandler())
        .basicAuthentication(userName, password).build();
  }
}
