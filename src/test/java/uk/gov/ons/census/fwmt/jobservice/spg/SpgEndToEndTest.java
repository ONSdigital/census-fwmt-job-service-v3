package uk.gov.ons.census.fwmt.jobservice.spg;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.consumer.queue.RmReceiver;
import uk.gov.ons.census.fwmt.jobservice.dto.rm.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.PutCaseRequest;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;
import uk.gov.ons.census.fwmt.jobservice.service.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.mapper.SpgMapper;

import javax.xml.bind.JAXBException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpgEndToEndTest {

  // Mocks
  @Mock private CometRestClient cometRestClient;

  // Services
  @Autowired GatewayEventManager gatewayEventManager;
  @Autowired SpgMapper spgMapper;
  private RmReceiver rmReceiver;
  private JobService jobService;

  // Internal
  @Autowired private SpgRequestBuilder spgRequestBuilder;
  @Autowired private SpgPutCaseBuilder spgPutCaseBuilder;
  @Autowired private ObjectMapper objectMapper;

  @BeforeEach
  public void setup() throws JAXBException {
    jobService = new JobService(spgMapper, cometRestClient, gatewayEventManager);
    rmReceiver = new RmReceiver(jobService, gatewayEventManager);
  }

  @Test
  public void testUnitDeliver() throws JsonProcessingException, GatewayException {
    FieldworkFollowup fieldworkFollowup = spgRequestBuilder.makeUnitDeliver();
    PutCaseRequest putCaseRequest = spgPutCaseBuilder.makeUnitDeliver();

    test(fieldworkFollowup, putCaseRequest);
  }

  @Test
  public void testUnitFollowup() throws JsonProcessingException, GatewayException {
    FieldworkFollowup fieldworkFollowup = spgRequestBuilder.makeUnitFollowup();
    PutCaseRequest putCaseRequest = spgPutCaseBuilder.makeUnitFollowup();

    test(fieldworkFollowup, putCaseRequest);
  }

  @Test
  public void testSite() throws JsonProcessingException, GatewayException {
    FieldworkFollowup fieldworkFollowup = spgRequestBuilder.makeSite();
    PutCaseRequest putCaseRequest = spgPutCaseBuilder.makeSite();

    test(fieldworkFollowup, putCaseRequest);
  }

  @Test
  public void testSecureSite() throws JsonProcessingException, GatewayException {
    FieldworkFollowup fieldworkFollowup = spgRequestBuilder.makeSecureSite();
    PutCaseRequest putCaseRequest = spgPutCaseBuilder.makeSecureSite();

    test(fieldworkFollowup, putCaseRequest);
  }

  private void test(FieldworkFollowup fieldworkFollowup, PutCaseRequest expected)
      throws JsonProcessingException, GatewayException {
    String message = objectMapper.writeValueAsString(fieldworkFollowup);
    ArgumentCaptor<PutCaseRequest> putArgument = ArgumentCaptor.forClass(PutCaseRequest.class);
    ArgumentCaptor<String> caseIdArgument = ArgumentCaptor.forClass(String.class);

    rmReceiver.receiveMessage(message);

    verify(cometRestClient).sendRequest(putArgument.capture(), caseIdArgument.capture());

    String caseId = caseIdArgument.getValue();
    assertEquals("exampleCaseId", caseId);

    PutCaseRequest put = putArgument.getValue();
    assertEquals(expected, put);
  }

}
