package uk.gov.ons.census.fwmt.jobservice.service.converter;

import java.util.HashMap;
import java.util.Map;

public class TransitionRulesLookup {

  private final Map<String, String[]> transitionRulesMap = new HashMap<>();

  public String[] getLookup(String cacheType, String rmRequest, String recordAge) {
    String requiredLookup = cacheType + "," + rmRequest + "," + recordAge;
    return transitionRulesMap.get(requiredLookup);
  }

  public void add (String transitionRuleSelector, String[] transitionRule) {
    transitionRulesMap.put(transitionRuleSelector, transitionRule);
  }
}
