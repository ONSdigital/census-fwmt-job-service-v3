package uk.gov.ons.census.fwmt.jobservice.consumer.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;

import java.io.IOException;
import java.util.UUID;

@Slf4j
//@Component
public class GatewayActionProducer {

  private final GatewayEventManager gatewayEventManager;
  private final RabbitTemplate rabbitTemplate;
  private final DirectExchange gatewayActionsExchange;
  private final ObjectMapper objectMapper;

  public GatewayActionProducer(
      GatewayEventManager gatewayEventManager,
      RabbitTemplate rabbitTemplate,
      @Qualifier("gatewayActionsExchange") DirectExchange gatewayActionsExchange,
      ObjectMapper objectMapper) {
    this.gatewayEventManager = gatewayEventManager;
    this.rabbitTemplate = rabbitTemplate;
    this.gatewayActionsExchange = gatewayActionsExchange;
    this.objectMapper = objectMapper;
  }

  @Retryable
  public void sendMessage(Object dto) throws GatewayException {
    String JSONJobRequest = convertToJSON(dto);

    Message gatewayMessage = convertJSONToMessage(JSONJobRequest);

    rabbitTemplate.convertAndSend(gatewayActionsExchange.getName(),
        GatewayActionsQueueConfig.GATEWAY_ACTIONS_ROUTING_KEY, gatewayMessage);
  }

  protected String convertToJSON(Object dto) throws GatewayException {
    String JSONJobRequest;
    try {
      JSONJobRequest = objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      String msg = "Failed to convert to JSON.";
      gatewayEventManager
          .triggerErrorEvent(this.getClass(), e, msg, getCaseId(dto), GatewayEventsConfig.FAILED_TO_MARSHALL_CANONICAL);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, ".", e);
    }
    return JSONJobRequest;
  }

  private String getCaseId(Object dto) {
    String caseId = "<NULL>";
    UUID uuid = null;
    if (dto instanceof CreateFieldWorkerJobRequest) {
      uuid = ((CreateFieldWorkerJobRequest) dto).getCaseId();
    } else if (dto instanceof CancelFieldWorkerJobRequest) {
      uuid = ((CancelFieldWorkerJobRequest) dto).getCaseId();
    } else if (dto instanceof UpdateFieldWorkerJobRequest) {
      uuid = ((UpdateFieldWorkerJobRequest) dto).getCaseId();
    }
    if (uuid != null)
      caseId = uuid.toString();
    return caseId;
  }

  public Message convertJSONToMessage(String messageJSON) throws GatewayException {
    MessageProperties messageProperties = new MessageProperties();
    messageProperties.setContentType("application/json");
    MessageConverter messageConverter = new Jackson2JsonMessageConverter();

    try {
      JsonNode object = objectMapper.readTree(messageJSON);
      return messageConverter.toMessage(object, messageProperties);
    } catch (IOException e) {
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Unable to convert JSON to message");
    }
  }
}
