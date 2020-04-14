package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCancelRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgCreateRouter;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgUpdateRouter;

@Slf4j
@Service
public class JobService {

  private final GatewayCacheService cacheService;
  private final GatewayEventManager eventManager;

  private final SpgCreateRouter createRouter;
  private final SpgUpdateRouter updateRouter;
  private final SpgCancelRouter cancelRouter;

  public JobService(GatewayCacheService cacheService, GatewayEventManager eventManager,
      SpgCreateRouter createRouter, SpgUpdateRouter updateRouter, SpgCancelRouter cancelRouter) {
    this.cacheService = cacheService;
    this.eventManager = eventManager;
    this.createRouter = createRouter;
    this.updateRouter = updateRouter;
    this.cancelRouter = cancelRouter;
  }

  public void process(FwmtActionInstruction ffu) throws GatewayException {
    GatewayCache cache = cacheService.getById(ffu.getCaseId());
    if (createRouter.isValid(ffu, cache)) {
      createRouter.routeUnsafe(ffu, cache);
    } else {
      updateRouter.route(ffu, cache, eventManager);
    }
  }

  public void process(FwmtCancelActionInstruction ffu) throws GatewayException {
    GatewayCache cache = cacheService.getById(ffu.getCaseId());
    cancelRouter.route(ffu, cache, eventManager);
  }
}
