package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.CaseType;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ce.CeUpdateConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_UPDATE_STORE;

@Qualifier("Update")
@Service
public class CeUpdateUnitPreCreateProcessor implements InboundProcessor<FwmtActionInstruction> {


  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private MessageCacheService messageCacheService;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.UPDATE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel("U")
      .build();

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.UPDATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("U")
          && cache == null;
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) {
    CeCasePatchRequest tmRequest;
    tmRequest = CeUpdateConverter.convertUnit(rmRequest);
    ObjectMapper tmRequestMapper = new ObjectMapper();
    String message;

    try {
      message = tmRequestMapper.writeValueAsString(tmRequest);
    } catch (JsonProcessingException processFailure){
      return;
    }
    messageCacheService.save(MessageCache.builder().caseId(rmRequest.getCaseId())
        .messageType(CaseType.UPDATE.toString()).message(message).build());

    eventManager
        .triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_UPDATE_STORE,
            "Case Ref", rmRequest.getCaseRef(),
            "TM Action", "Update received before create case. Update has been stored.");
  }
}
