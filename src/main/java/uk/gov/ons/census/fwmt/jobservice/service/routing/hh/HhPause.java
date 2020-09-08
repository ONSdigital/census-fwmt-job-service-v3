package uk.gov.ons.census.fwmt.jobservice.service.routing.hh;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CREATE_TM_JOB;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import uk.gov.ons.census.fwmt.common.data.tm.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.converter.hh.HhPauseConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

@Qualifier("Pause")
@Service
public class HhPause implements InboundProcessor<FwmtActionInstruction> {

  private static final String PROCESSING = "PROCESSING";
  
  public static final String COMET_PAUSE_PRE_SENDING = "COMET_PAUSE_PRE_SENDING";

  public static final String COMET_PAUSE_ACK = "COMET_PAUSE_ACK";

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.PAUSE.toString())
      .surveyName("CENSUS")
      .addressType("HH")
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.PAUSE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("HH")
          && (cache != null && cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), PROCESSING,
        "type", "HH Pause Case",
        "action", "Pause");
    
    CasePauseRequest tmRequest = HhPauseConverter.buildPause(rmRequest);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_PAUSE_PRE_SENDING,
        "Case Ref", rmRequest.getCaseRef());

    ResponseEntity<Void> response = cometRestClient.sendPause(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(response, rmRequest.getCaseId(), "Pause", FAILED_TO_CREATE_TM_JOB);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_PAUSE_ACK,
        "Case Ref", rmRequest.getCaseRef(),
        "Response Code", response.getStatusCode().name());
  }
}
