//package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Qualifier;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Service;
//import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
//import uk.gov.ons.census.fwmt.common.error.GatewayException;
//import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
//import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
//import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
//import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
//import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
//import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
//import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
//import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
//import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
//
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_ACK;
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_CLOSE_PRE_SENDING;
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_REOPEN_PRE_SENDING;
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.COMET_REOPEN_ACK;
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_CLOSE_TM_JOB;
//import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.FAILED_TO_REOPEN_TM_JOB;
//
//@Qualifier("Create")
//@Service
//public class CeSwitchCreateProcessor implements InboundProcessor<FwmtActionInstruction> {
//
//  @Autowired
//  private CometRestClient cometRestClient;
//
//  @Autowired
//  private GatewayEventManager eventManager;
//
//  @Autowired
//  private RoutingValidator routingValidator;
//
//  @Autowired
//  private GatewayCacheService cacheService;
//
//  private static ProcessorKey key = ProcessorKey.builder()
//      .actionInstruction(ActionInstructionType.SWITCH_CE_TYPE.toString())
//      .surveyName("CENSUS")
//      .addressType("CE")
//      .build();
//
//  @Override
//  public ProcessorKey getKey() {
//    return key;
//  }
//
//  @Override
//  public boolean isValid(FwmtActionInstruction rmRequest, GatewayCache cache) {
//    try {
//      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
//          && rmRequest.getSurveyName().equals("CENSUS")
//          && rmRequest.getAddressType().equals("CE")
//          && cache != null
//          && (cache.getCaseId().equals(rmRequest.getCaseId()) && cache.existsInFwmt);
//    } catch (NullPointerException e) {
//      return false;
//    }
//  }
//
//  @Override
//  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
//    ReopenCaseRequest tmRequest = new ReopenCaseRequest();
//
//    tmRequest.setSurveyType(rmRequest.getSurveyType());
//    tmRequest.setId(rmRequest.getCaseId());
//
//    eventManager.triggerEvent(String.valueOf(tmRequest.getId()), COMET_CLOSE_PRE_SENDING,  "Survey Type",
//        tmRequest.getSurveyType().toString());
//
//    ResponseEntity<Void> closeResponse = cometRestClient.sendClose(rmRequest.getCaseId());
//    routingValidator.validateResponseCode(closeResponse, rmRequest.getCaseId(), "Close", FAILED_TO_CLOSE_TM_JOB);
//
//    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_CLOSE_ACK,  "Survey Type",
//        tmRequest.getSurveyType().toString());
//
//    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_REOPEN_PRE_SENDING,  "Survey Type",
//        tmRequest.getSurveyType().toString());
//
//    ResponseEntity<Void> reopenResponse = cometRestClient.sendReopen(tmRequest, rmRequest.getCaseId());
//    routingValidator.validateResponseCode(reopenResponse, rmRequest.getCaseId(), "Reopen", FAILED_TO_REOPEN_TM_JOB);
//
//    eventManager.triggerEvent(String.valueOf(rmRequest.getCaseId()), COMET_REOPEN_ACK,  "Survey Type",
//        tmRequest.getSurveyType().toString());
//  }
//}
