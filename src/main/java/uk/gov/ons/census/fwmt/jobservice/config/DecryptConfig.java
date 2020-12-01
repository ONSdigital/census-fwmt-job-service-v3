package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

@Configuration
public class DecryptConfig {

  public final String password;
  public final Resource privateKey;

  public DecryptConfig(
      @Value("${totalmobile.username}") String password,
      @Value("${totalmobile.password}") Resource privateKey) {
    this.privateKey = privateKey;
    this.password = password;
  }
}
