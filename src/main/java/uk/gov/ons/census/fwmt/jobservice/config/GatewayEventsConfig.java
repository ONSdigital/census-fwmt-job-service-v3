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

  // from the job service v3
  public static final String COMET_CREATE_PRE_SENDING = "COMET_CREATE_PRE_SENDING";
  public static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  public static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";
  public static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";
  public static final String COMET_CLOSE_PRE_SENDING = "COMET_CLOSE_PRE_SENDING";
  public static final String COMET_CLOSE_ACK = "COMET_CLOSE_ACK";
  public static final String COMET_UPDATE_PRE_SENDING = "COMET_UPDATE_PRE_SENDING";
  public static final String COMET_UPDATE_ACK = "COMET_UPDATE_ACK";
  public static final String TM_SERVICE_UP = "TM_SERVICE_UP";
  public static final String RABBIT_QUEUE_UP = "RABBIT_QUEUE_UP";
  // public static final String REDIS_SERVICE_UP = "REDIS_SERVICE_UP";

  public static final String FAILED_TM_AUTHENTICATION = "FAILED_TM_AUTHENTICATION";
  public static final String FAILED_TO_CREATE_TM_JOB = "FAILED_TO_CREATE_TM_JOB";
  public static final String FAILED_TO_CANCEL_TM_JOB = "FAILED_TO_CANCEL_TM_JOB";
  public static final String FAILED_TO_UPDATE_TM_JOB = "FAILED_TO_UPDATE_TM_JOB";
  public static final String TM_SERVICE_DOWN = "TM_SERVICE_DOWN";
  public static final String RABBIT_QUEUE_DOWN = "RABBIT_QUEUE_DOWN";
  public static final String CASE_NOT_FOUND = "CASE_NOT_FOUND";
  // public static final String REDIS_SERVICE_DOWN = "REDIS_SERVICE_DOWN";

  // from the rm adapter
  public static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  public static final String RM_UPDATE_REQUEST_RECEIVED = "RM_UPDATE_REQUEST_RECEIVED";
  public static final String RM_CANCEL_REQUEST_RECEIVED = "RM_CANCEL_REQUEST_RECEIVED";

  public static final String INVALID_ACTION_INSTRUCTION = "INVALID_ACTION_INSTRUCTION";
  public static final String FAILED_TO_UNMARSHALL_ACTION_INSTRUCTION = "FAILED_TO_UNMARSHALL_ACTION_INSTRUCTION";
  //public static final String RABBIT_QUEUE_DOWN = "RABBIT_QUEUE_DOWN";
  //public static final String REDIS_SERVICE_DOWN = "REDIS_SERVICE_DOWN";

  // internal routing
  public static final String ROUTING_FAILED = "ROUTING_FAILED";

  public static final String CONVERT_SPG_UNIT_UPDATE_TO_CREATE = "CONVERT_SPG_UNIT_UPDATE_TO_CREATE";

  private final boolean useJsonLogging;

  public GatewayEventsConfig(@Value("#{'${logging.profile}' == 'CLOUD'}") boolean useJsonLogging) {
    this.useJsonLogging = useJsonLogging;
  }

  @Bean
  public GatewayEventManager gatewayEventManager() {
    GatewayEventManager gatewayEventManager = new GatewayEventManager();
    gatewayEventManager.setSource(Application.APPLICATION_NAME);
    gatewayEventManager.addEventTypes(new String[] {
        // from both
        RABBIT_QUEUE_UP,
        // from the rm adapter
        RM_CREATE_REQUEST_RECEIVED, RM_UPDATE_REQUEST_RECEIVED, RM_CANCEL_REQUEST_RECEIVED,
        // from the job service v3
        COMET_CREATE_PRE_SENDING, COMET_CREATE_ACK, COMET_CANCEL_PRE_SENDING, COMET_CANCEL_ACK,
        COMET_UPDATE_PRE_SENDING, COMET_UPDATE_ACK, TM_SERVICE_UP, CONVERT_SPG_UNIT_UPDATE_TO_CREATE,
    });
    gatewayEventManager.addErrorEventTypes(new String[] {
        // from both
        RABBIT_QUEUE_DOWN, // REDIS_SERVICE_DOWN,
        // from the rm adapter
        INVALID_ACTION_INSTRUCTION, FAILED_TO_UNMARSHALL_ACTION_INSTRUCTION,
        // from the job service v3
        FAILED_TM_AUTHENTICATION, FAILED_TO_CREATE_TM_JOB, FAILED_TO_CANCEL_TM_JOB, FAILED_TO_UPDATE_TM_JOB,
        TM_SERVICE_DOWN,
        CASE_NOT_FOUND,
        // internal routing
        ROUTING_FAILED,

    });

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
