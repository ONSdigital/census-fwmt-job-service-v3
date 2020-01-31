package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.jobservice.rest.client.CometRestClientResponseErrorHandler;

@Configuration
public class TMRestTemplate {

  private final String userName;
  private final String password;

  public TMRestTemplate(
      @Value("${totalmobile.username}") String userName,
      @Value("${totalmobile.password}") String password) {
    this.userName = userName;
    this.password = password;
  }

  @Bean(name = "tm")
  public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
    return restTemplateBuilder.errorHandler(new CometRestClientResponseErrorHandler())
        .basicAuthentication(userName, password).build();
  }
}
