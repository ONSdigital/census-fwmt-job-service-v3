package uk.gov.ons.census.fwmt.jobservice.service;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.SpgRoutingEngine;

@Slf4j
@Service
public class JobService {

  private final GatewayCacheService cacheService;
  private final GatewayEventManager eventManager;
  private final SpgRoutingEngine spgRoutingEngine;

  public JobService(GatewayCacheService cacheService, SpgRoutingEngine spgRoutingEngine, GatewayEventManager eventManager) {
    this.cacheService = cacheService;
    this.spgRoutingEngine = spgRoutingEngine;
    this.eventManager = eventManager;
  }

  public void process(FieldworkFollowup ffu) throws GatewayException {
    GatewayCache cache = cacheService.getById(ffu.getCaseId());
    spgRoutingEngine.route(ffu, cache);
  }
}
