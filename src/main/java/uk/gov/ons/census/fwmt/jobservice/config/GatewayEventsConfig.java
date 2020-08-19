package uk.gov.ons.census.fwmt.jobservice.config;

import com.godaddy.logging.LoggingConfigs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.Application;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.function.Function;

@Configuration
public class GatewayEventsConfig {

  public static final String COMET_CREATE_PRE_SENDING = "COMET_CREATE_PRE_SENDING";
  public static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";
  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";
  public static final String COMET_CLOSE_PRE_SENDING = "COMET_CLOSE_PRE_SENDING";
  public static final String COMET_CLOSE_ACK = "COMET_CLOSE_ACK";
  public static final String COMET_REOPEN_PRE_SENDING = "COMET_REOPEN_PRE_SENDING";
  public static final String COMET_REOPEN_ACK = "COMET_REOPEN_ACK";
  public static final String COMET_UPDATE_PRE_SENDING = "COMET_UPDATE_PRE_SENDING";
  public static final String COMET_UPDATE_ACK = "COMET_UPDATE_ACK";
  public static final String COMET_DELETE_PRE_SENDING = "COMET_DELETE_PRE_SENDING";
  public static final String COMET_DELETE_ACK = "COMET_DELETE_ACK";

  public static final String FAILED_TO_CREATE_TM_JOB = "FAILED_TO_CREATE_TM_JOB";
  public static final String FAILED_TO_CANCEL_TM_JOB = "FAILED_TO_CANCEL_TM_JOB";
  public static final String FAILED_TO_CLOSE_TM_JOB = "FAILED_TO_CLOSE_TM_JOB";
  public static final String FAILED_TO_REOPEN_TM_JOB = "FAILED_TO_REOPEN_TM_JOB";
  public static final String FAILED_TO_UPDATE_TM_JOB = "FAILED_TO_UPDATE_TM_JOB";
  public static final String CASE_NOT_FOUND = "CASE_NOT_FOUND";
  public static final String INCORRECT_SWITCH_SURVEY_TYPE = "INCORRECT_SWITCH_SURVEY_TYPE";

  public static final String CONVERT_SPG_UNIT_UPDATE_TO_CREATE = "CONVERT_SPG_UNIT_UPDATE_TO_CREATE";

  private final boolean useJsonLogging;

  public GatewayEventsConfig(@Value("#{'${logging.profile}' == 'CLOUD'}") boolean useJsonLogging) {
    this.useJsonLogging = useJsonLogging;
  }

  @Bean
  public GatewayEventManager gatewayEventManager() {
    GatewayEventManager gatewayEventManager = new GatewayEventManager();
    gatewayEventManager.setSource(Application.APPLICATION_NAME);

    return gatewayEventManager;
  }

  @PostConstruct
  public void initJsonLogging() {
    HashMap<Class<?>, Function<Object, String>> customMappers = new HashMap<>();
    customMappers.put(LocalTime.class, Object::toString);
    customMappers.put(LocalDateTime.class, Object::toString);

    LoggingConfigs configs;

    if (useJsonLogging) {
      configs = LoggingConfigs.builder().customMapper(customMappers).build().useJson();
    } else {
      configs = LoggingConfigs.builder().customMapper(customMappers).build();
    }
    LoggingConfigs.setCurrent(configs);
  }
}
