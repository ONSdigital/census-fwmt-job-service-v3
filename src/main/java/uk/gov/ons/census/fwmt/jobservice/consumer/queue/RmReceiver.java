package uk.gov.ons.census.fwmt.jobservice.consumer.queue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.dto.rm.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

@Slf4j
@Component
public class RmReceiver {
  private final JobService jobService;
  private final GatewayEventManager gatewayEventManager;
  private final JAXBContext jaxbContext;
  private final Unmarshaller unmarshaller;
  private final ObjectMapper objectMapper;

  public RmReceiver(JobService jobService, GatewayEventManager gatewayEventManager) throws JAXBException {
    this.jobService = jobService;
    this.gatewayEventManager = gatewayEventManager;
    this.jaxbContext = JAXBContext.newInstance(FieldworkFollowup.class);
    this.unmarshaller = jaxbContext.createUnmarshaller();
    this.objectMapper = new ObjectMapper();
    //this.unmarshaller.setProperty(, "application/json");
  }

  public void receiveMessage(String message) throws GatewayException, JsonProcessingException {
    //StreamSource source = new StreamSource(new StringReader(message));

    FieldworkFollowup fieldworkFollowup;
    try {
      //fieldworkFollowup = unmarshaller.unmarshal(source, FieldworkFollowup.class).getValue();
      fieldworkFollowup = objectMapper.readValue(message, FieldworkFollowup.class);
    } catch (JsonProcessingException e) {
      // TODO proper error handling
      throw e;
    }

    jobService.handleMessage(fieldworkFollowup);
  }

}

