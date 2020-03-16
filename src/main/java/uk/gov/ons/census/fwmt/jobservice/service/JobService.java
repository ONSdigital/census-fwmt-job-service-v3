package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgRouter;

import java.util.List;

@Slf4j
@Service
public class JobService {

  private final GatewayCacheService cacheService;
  private final SpgRouter router;
  private final GatewayEventManager eventManager;

  public JobService(GatewayCacheService cacheService, SpgRouter router, GatewayEventManager eventManager) {
    this.cacheService = cacheService;
    this.router = router;
    this.eventManager = eventManager;
  }

  public void process(FieldworkFollowup ffu) throws GatewayException {
    GatewayCache cache = cacheService.getById(ffu.getCaseId());
    router.route(ffu, cache, eventManager);
  }
}
