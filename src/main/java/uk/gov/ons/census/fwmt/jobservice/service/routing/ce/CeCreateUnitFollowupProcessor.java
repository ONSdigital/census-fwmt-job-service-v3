package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.CaseType;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.comet.CometRestClient;
import uk.gov.ons.census.fwmt.jobservice.rabbit.RmFieldRepublishProducer;
import uk.gov.ons.census.fwmt.jobservice.service.CeFollowUpSchedulingService;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RoutingValidator;
import uk.gov.ons.census.fwmt.jobservice.service.routing.common.CeCreateCommonProcessor;

@Qualifier("Create")
@Service
public class CeCreateUnitFollowupProcessor implements InboundProcessor<FwmtActionInstruction> {
  @Autowired
  private CometRestClient cometRestClient;

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RoutingValidator routingValidator;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private CeFollowUpSchedulingService config;

  @Autowired
  private RmFieldRepublishProducer rmFieldRepublishProducer;

  @Autowired
  private MessageCacheService messageCacheService;

  @Autowired
  private CeCreateCommonProcessor ceCreateCommonProcessor;

  private static ProcessorKey key = ProcessorKey.builder()
      .actionInstruction(ActionInstructionType.CREATE.toString())
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
      return rmRequest.getActionInstruction() == ActionInstructionType.CREATE
          && rmRequest.getSurveyName().equals("CENSUS")
          && rmRequest.getAddressType().equals("CE")
          && rmRequest.getAddressLevel().equals("U")
          && !rmRequest.isHandDeliver()
          && (cache == null
              || !cache.existsInFwmt)
          && config.isInFollowUp();
    } catch (NullPointerException e) {
      return false;
    }
  }

  // TODO what do we do with followUpService
  // TODO add test for secure
  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
    if (!messageCacheService.doesCaseIdAndMessageTypeExist(rmRequest.getCaseId(), CaseType.CANCEL.toString())) {
      if (cacheService.doesEstabUprnAndTypeExist(rmRequest.getEstabUprn(), 1)) {
        FwmtActionInstruction ceSwitch = new FwmtActionInstruction();

        ceSwitch.setActionInstruction(ActionInstructionType.SWITCH_CE_TYPE);
        ceSwitch.setSurveyName("CENSUS");
        ceSwitch.setAddressType("CE");
        ceSwitch.setAddressLevel(null);
        ceSwitch.setCaseId(cacheService.getEstabCaseId(rmRequest.getEstabUprn()));
        ceSwitch.setSurveyType(SurveyType.CE_SITE);

        rmFieldRepublishProducer.republish(ceSwitch);
      }

      String converterMethod;
      if (rmRequest.isSecureEstablishment()) {
        converterMethod = "convertCeUnitFollowupSecure";
      } else {
        converterMethod = "convertCeUnitFollowup";
      }
      ceCreateCommonProcessor.commonProcessor(rmRequest, converterMethod, cache, 3, true);
    } else {
      ceCreateCommonProcessor.preCreateCancel(rmRequest, 3);
    }
  }
}
