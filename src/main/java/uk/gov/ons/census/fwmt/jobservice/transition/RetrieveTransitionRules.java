package uk.gov.ons.census.fwmt.jobservice.transition;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRule;
import uk.gov.ons.census.fwmt.jobservice.service.converter.TransitionRulesLookup;

import java.util.Date;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.ROUTING_FAILED;

@Slf4j
@Component
public class RetrieveTransitionRules {

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private TransitionRulesLookup transitionRulesLookup;

  private TransitionRule returnedRules;

  public TransitionRule collectTransitionRules(GatewayCache cache, String actionRequest, String caseId,
      Date messageReceivedTime) throws GatewayException {
    String cacheType;
    String recordAge;

    if (cache == null) {
      cacheType = "EMPTY";
      recordAge = "NEWER";
    } else {
      //TODO - call to checkRecordAge() will be here once the code is there
      cacheType = cache.lastActionInstruction;
      recordAge = checkRecordAge(cache, messageReceivedTime);
    }

    TransitionRule returnedRules = transitionRulesLookup.getLookup(cacheType, actionRequest, recordAge);

    if (returnedRules == null) {
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a rule for the create request from RM",
          String.valueOf(caseId), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,
          "Could not find a rule for the create request from RM", cache);
    }
    return returnedRules;
  }

  public String checkRecordAge(GatewayCache gatewayCache, Date messageReceivedTime) {
    String recordAge = "";
    if (gatewayCache.getLastActionTime().after(messageReceivedTime) || gatewayCache.getLastActionTime().equals(messageReceivedTime)) {
      recordAge = "OLDER";
    } else if (gatewayCache.getLastActionTime().before(messageReceivedTime)) {
      recordAge = "NEWER";
    }
    return recordAge;
  }
}