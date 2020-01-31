package uk.gov.ons.census.fwmt.jobservice.message;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ons.census.fwmt.canonical.v1.CancelFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.CreateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.canonical.v1.UpdateFieldWorkerJobRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.service.JobService;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

// TODO Some stubs were disabled for being redundant. This may require later investigation, so they have been commented out.
// TODO Along with this, some fields were left written but un-read, and have also been commented out.

@RunWith(MockitoJUnitRunner.class)
public class JobServiceMessageReceiverTest {

  @InjectMocks
  private GatewayActionsReceiver messageReceiver;

  @Mock
  private JobService jobService;

  //@Mock
  //private ObjectMapper mapper = new ObjectMapper();

  @Mock
  private GatewayEventManager gatewayEventManager;

  @Mock
  private MessageConverter messageConverter = new MessageConverter();

  @Mock
  private CreateFieldWorkerJobRequest createFieldWorkerJobRequest = new CreateFieldWorkerJobRequest();

  @Mock
  private CancelFieldWorkerJobRequest cancelFieldWorkerJobRequest = new CancelFieldWorkerJobRequest();

  @Mock
  private UpdateFieldWorkerJobRequest updateFieldWorkerJobRequest = new UpdateFieldWorkerJobRequest();

  @Test
  public void receiveMessageCreate() throws GatewayException, IOException {
    JSONObject json = new JSONObject();
    JSONObject address = new JSONObject();
    json.put("actionType", "Create");
    json.put("caseId", "a48bf28e-e7f4-4467-a9fb-e000b6a55676");
    json.put("caseReference", "caseReference");
    json.put("caseType", "Household");
    json.put("establishmentType", "estabType");
    json.put("coordinatorId", "coordId");
    json.put("surveyType", "HH");
    json.put("mandatoryResource", "1234");
    address.put("line1", "1 Station Road");
    address.put("townName", "Town");
    address.put("postCode", "AB1 2CD");
    address.put("latitude", "1234.56");
    address.put("longitude", "2345.67");
    address.put("oa", "oaTest");
    json.put("address", address);
    json.put("gatewayType", "Create");

    String message = json.toString();

    //ObjectMapper objectMapper = new ObjectMapper();
    //JsonNode jsonNode = objectMapper.readTree(message);

    when(messageConverter.convertMessageToDTO(CreateFieldWorkerJobRequest.class, message))
        .thenReturn(createFieldWorkerJobRequest);

    //when(mapper.readTree(message)).thenReturn(jsonNode);

    messageReceiver.receiveMessage(message);

    Mockito.verify(jobService).createJob(any());
    Mockito.verify(jobService, never()).cancelJob(any());
  }

  @Test
  public void receiveMessageCancel() throws GatewayException, IOException {
    JSONObject json = new JSONObject();
    json.put("actionType", "Cancel");
    json.put("caseId", "8ed3fc08-e95f-44db-a6d7-cde4e76a6182");
    json.put("reason", "incorrect address");
    json.put("gatewayType", "Cancel");

    String message = json.toString();

    //ObjectMapper objectMapper = new ObjectMapper();
    //JsonNode jsonNode = objectMapper.readTree(message);

    when(messageConverter.convertMessageToDTO(CancelFieldWorkerJobRequest.class, message))
        .thenReturn(cancelFieldWorkerJobRequest);

    //when(mapper.readTree(message)).thenReturn(jsonNode);

    messageReceiver.receiveMessage(message);

    Mockito.verify(jobService).cancelJob(any());
  }

  @Test
  public void receiveNisraMessageCreate() throws GatewayException, IOException {
    JSONObject json = new JSONObject();
    JSONObject address = new JSONObject();
    json.put("actionType", "Create");
    json.put("jobIdentity", "1234");
    json.put("surveyType", "HH");
    json.put("preallocatedJob", "true");
    json.put("mandatoryResourceAuthNo", "1234");
    json.put("dueDate", "20180216");
    address.put("line1", "886");
    address.put("line2", "Prairie Rose");
    address.put("line3", "Trail");
    address.put("line4", "RU");
    address.put("townName", "Borodinskiy");
    address.put("postCode", "188961");
    address.put("latitude", "61.7921776");
    address.put("longitude", "34.3739957");
    json.put("address", address);
    json.put("mandatoryResource", "testFieldOfficerId");
    json.put("gatewayType", "Create");

    String message = json.toString();

    //ObjectMapper objectMapper = new ObjectMapper();
    //JsonNode jsonNode = objectMapper.readTree(message);

    when(messageConverter.convertMessageToDTO(CreateFieldWorkerJobRequest.class, message))
        .thenReturn(createFieldWorkerJobRequest);

    //when(mapper.readTree(message)).thenReturn(jsonNode);

    messageReceiver.receiveMessage(message);

    Mockito.verify(jobService).createJob(any());
    Mockito.verify(jobService, never()).cancelJob(any());
  }

  @Test(expected = GatewayException.class)
  public void receiveBadProcessMessage() throws GatewayException, IOException {
    JSONObject json = new JSONObject();
    json.put("actionType", "");
    json.put("caseId", "8ed3fc08-e95f-44db-a6d7-cde4e76a6182");
    json.put("gatewayType", "Incorrect");

    String message = json.toString();

    //ObjectMapper objectMapper = new ObjectMapper();
    //JsonNode jsonNode = objectMapper.readTree(message);

    //when(mapper.readTree(message)).thenReturn(jsonNode);

    messageReceiver.receiveMessage(message);
  }

  @Test
  public void receiveMessageUpdate() throws GatewayException, IOException {
    JSONObject json = new JSONObject();
    json.put("actionType", "update");
    json.put("caseId", "f98e469e-1727-4ef8-bc87-354a7ebdf1de");
    json.put("gatewayType", "Update");

    String message = json.toString();

    //ObjectMapper objectMapper = new ObjectMapper();
    //JsonNode jsonNode = objectMapper.readTree(message);

    when(messageConverter.convertMessageToDTO(UpdateFieldWorkerJobRequest.class, message))
        .thenReturn(updateFieldWorkerJobRequest);

    //when(mapper.readTree(message)).thenReturn(jsonNode);

    messageReceiver.receiveMessage(message);

    Mockito.verify(jobService).updateJob(any());
  }
}
