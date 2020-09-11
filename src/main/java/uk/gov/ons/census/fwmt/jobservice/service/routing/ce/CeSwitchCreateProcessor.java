package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonSwitchConverter;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_REOPEN_ACK;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_REOPEN_PRE_SENDING;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CLOSE_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_REOPEN_TM_JOB;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.INCORRECT_SWITCH_SURVEY_TYPE;

@Qualifier("Create")
@Service
public class CeSwitchCreateProcessor implements InboundProcessor<FwmtActionInstruction> {

  private static final ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.SWITCH_CE_TYPE.toString())
      .surveyName("CENSUS")
      .addressType("CE")
      .addressLevel(null)
      .build();

  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Override
  public ProcessorKey getKey() {
    return key;
  }

  @Override
  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
    try {
      return rmRequest.getActionInstruction() == ActionInstructionType.SWITCH_CE_TYPE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel() == null
          && ((cache != null)
          && (cache.existsInFwmt));
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
    ReopenCaseRequest tmRequest;

    if (rmRequest.getSurveyType().equals(SurveyType.CE_EST_D)) {
      cache.setType(1);
      tmRequest = CommonSwitchConverter.convertEstabDeliver(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_EST_F)) {
      cache.setType(1);
      tmRequest = CommonSwitchConverter.converEstabFollowup(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_SITE) && cache.getType() != 2) {
      cache.setType(2);
      tmRequest = CommonSwitchConverter.convertSite(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_UNIT_D)) {
      cache.setType(3);
      tmRequest = CommonSwitchConverter.convertUnitDeliver(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else if (rmRequest.getSurveyType().equals(SurveyType.CE_UNIT_F)) {
      cache.setType(3);
      tmRequest = CommonSwitchConverter.converUnitFollowup(rmRequest);
      processSwitch(cache, rmRequest, tmRequest);
    } else {
      eventManager.triggerErrorEvent(this.getClass(), "Not a recognised CE Switch SurveyType",
          String.valueOf(rmRequest.getCaseId()), INCORRECT_SWITCH_SURVEY_TYPE);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Incorrect CE Switch survey type");
    }
  }

  private void processSwitch(GatewayCache cache, FwmtActionInstruction rmRequest, ReopenCaseRequest tmRequest)
      throws GatewayException {

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_PRE_SENDING, "Survey Type",
        rmRequest.getSurveyType().toString());

    ResponseEntity<Void> closeResponse = cometRestClient.sendClose(rmRequest.getCaseId());
    routingValidator.validateResponseCode(closeResponse, rmRequest.getCaseId(), "Close", FAILED_TO_CLOSE_TM_JOB);

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_ACK, "Survey Type",
        tmRequest.getSurveyType().toString());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_REOPEN_PRE_SENDING, "Survey Type",
        tmRequest.getSurveyType().toString());

    ResponseEntity<Void> reopenResponse = cometRestClient.sendReopen(tmRequest, rmRequest.getCaseId());
    routingValidator.validateResponseCode(reopenResponse, rmRequest.getCaseId(), "Reopen", FAILED_TO_REOPEN_TM_JOB);

    cacheService.save(cache.toBuilder().build());

    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_REOPEN_ACK, "Survey Type",
        tmRequest.getSurveyType().toString());
  }
}