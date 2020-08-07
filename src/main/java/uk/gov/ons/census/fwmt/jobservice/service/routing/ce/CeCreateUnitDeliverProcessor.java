package uk.gov.ons.census.fwmt.jobservice.service.routing.ce;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.CREATE_FOR_CASE_ALREADY_EXISTS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import uk.gov.ons.census.fwmt.common.data.tm.CeCasePatchRequest;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.ActionInstructionType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.ConvertCachedMessage;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;
import uk.gov.ons.census.fwmt.jobservice.rabbit.RmFieldRepublishProducer;
import uk.gov.ons.census.fwmt.jobservice.service.GatewayCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.MessageCacheService;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;
import uk.gov.ons.census.fwmt.jobservice.service.routing.common.CeCreateCommonProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.routing.common.CeUpdateCommonProcessor;

import java.sql.Timestamp;

@Qualifier("Create")
@Service
public class CeCreateUnitDeliverProcessor implements InboundProcessor<FwmtActionInstruction> {

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private GatewayCacheService cacheService;

  @Autowired
  private MessageCacheService messageCacheService;

  @Autowired
  private RmFieldRepublishProducer rmFieldRepublishProducer;

  @Autowired
  private CeCreateCommonProcessor ceCreateCommonProcessor;

  @Autowired
  private ConvertCachedMessage convertCachedMessage;

  @Autowired
  private CeUpdateCommonProcessor ceUpdateCommonProcessor;

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
          && rmRequest.isHandDeliver()
          && (cache == null
          || !cache.existsInFwmt);
    } catch (NullPointerException e) {
      return false;
    }
  }

  @Override
  public void process(FwmtActionInstruction rmRequest, GatewayCache cache) throws GatewayException {
    if (cacheService.doesEstabUprnAndTypeExist(rmRequest.getEstabUprn(), 1)) {
      FwmtActionInstruction ceSwitch = new FwmtActionInstruction();

      ceSwitch.setActionInstruction(ActionInstructionType.SWITCH_CE_TYPE);
      ceSwitch.setSurveyName("CENSUS");
      ceSwitch.setAddressType("CE");
      ceSwitch.setAddressLevel(null);
      ceSwitch.setCaseId(cacheService.getUprnCaseId(rmRequest.getEstabUprn()));
      ceSwitch.setSurveyType(SurveyType.CE_SITE);

      rmFieldRepublishProducer.republish(ceSwitch);
    }

    if (cacheService.getById(rmRequest.getCaseId()) == null) {
      switch (messageCacheService.getMessageTypeForId(rmRequest.getCaseId())) {
      case "Cancel":
        ceCreateCommonProcessor.preCreateCancel(rmRequest, 3);
      case "Update":
        ceUpdateCommonProcessor.processPreUpdate(rmRequest, cache);
        break;
      case "": case "Create":
        ceCreateCommonProcessor.commonProcessor(rmRequest, cache, 3, false);
      default:
        break;
      }
    } else {
      eventManager.triggerErrorEvent(this.getClass(), "Create already exists for case",
          String.valueOf(rmRequest.getCaseId()), CREATE_FOR_CASE_ALREADY_EXISTS);
    }
  }
}
