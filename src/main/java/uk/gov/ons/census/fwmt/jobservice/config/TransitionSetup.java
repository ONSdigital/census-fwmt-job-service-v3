package uk.gov.ons.census.fwmt.jobservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRulesLookup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static java.nio.charset.StandardCharsets.UTF_8;

@Configuration
public class TransitionSetup {
  @Autowired
  ResourceLoader resourceLoader;

  @Value(value = "${jobservice.transitionRules.path}")
  private String transitionRules;

  @Bean
  public TransitionRulesLookup buildTransitionRuleLookup() throws GatewayException {
    String transitionLine;
    Resource resource = resourceLoader.getResource(transitionRules);

    TransitionRulesLookup transitionRulesLookup = new TransitionRulesLookup();

    try (BufferedReader in = new BufferedReader(new InputStreamReader(resource.getInputStream(), UTF_8))) {
      while ((transitionLine = in.readLine()) != null) {
        String[] lookup = transitionLine.split("\\|");
        String[] transitionSelector = new String[] {lookup[0], lookup[1], lookup[2]};
        String transitionSelectorRule = String.join(",", transitionSelector);
        transitionRulesLookup.add(transitionSelectorRule, lookup[3].split(","));
      }
    }catch (IOException e) {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Cannot process transition rule lookup");
    }
    return transitionRulesLookup;
  }
}
