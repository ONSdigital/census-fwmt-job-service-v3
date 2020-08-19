package uk.gov.ons.census.fwmt.jobservice.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtCancelActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.processor.InboundProcessor;
import uk.gov.ons.census.fwmt.jobservice.service.processor.ProcessorKey;

@Slf4j
@Service
public class JobService {

  @Autowired
  private GatewayCacheService cacheService;
  
  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  @Qualifier("CreateProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> createProcessorMap;

  @Autowired
  @Qualifier("UpdateProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> updateProcessorMap;

  @Autowired
  @Qualifier("CancelProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtCancelActionInstruction>>> cancelProcessorMap;

  @Autowired
  @Qualifier("PauseProcessorMap")
  private Map<ProcessorKey, List<InboundProcessor<FwmtActionInstruction>>> pauseProcessorMap;

  public static final String ROUTING_FAILED = "ROUTING_FAILED";

  public void processCreate(FwmtActionInstruction rmRequest) throws GatewayException {
    final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());
    ProcessorKey key = ProcessorKey.buildKey(rmRequest);

    List<InboundProcessor<FwmtActionInstruction>> processors = createProcessorMap.get(key).stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());
    if (processors.size()==0){
      //TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a CREATE processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Could not find a CREATE processor for request from RM", rmRequest, cache);
    }
    if (processors.size()>1){
      //TODO throw routing error  & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple CREATE processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Found multiple CREATE processors for request from RM", rmRequest, cache);
    }
    processors.get(0).process(rmRequest, cache);    
  }

  public void processUpdate(FwmtActionInstruction rmRequest) throws GatewayException {
    final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());
    ProcessorKey key = ProcessorKey.buildKey(rmRequest);
    List<InboundProcessor<FwmtActionInstruction>> processors = updateProcessorMap.get(key).stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());
    if (processors.size()==0){
      //TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a UPDATE processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Could not find a UPDATE processor for request from RM", rmRequest, cache);
    }
    if (processors.size()>1){
      //TODO throw routing error  & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple UPDATE processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Found multiple UPDATE processors for request from RM", rmRequest, cache);
    }
    processors.get(0).process(rmRequest, cache);
  }

  public void processCancel(FwmtCancelActionInstruction rmRequest) throws GatewayException {
      final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());
    ProcessorKey key = ProcessorKey.buildKey(rmRequest);
    List<InboundProcessor<FwmtCancelActionInstruction>> processors = cancelProcessorMap.get(key).stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());
    if (processors.size()==0){
      //TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a CANCEL processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Could not find a CANCEL processor for request from RM", rmRequest, cache);
    }
    if (processors.size()>1){
      //TODO throw routing error  & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple CANCEL processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Found multiple CANCEL processors for request from RM", rmRequest, cache);
    }
    processors.get(0).process(rmRequest, cache);
  }

  public void processPause(FwmtActionInstruction rmRequest) throws GatewayException {
    final GatewayCache cache = cacheService.getById(rmRequest.getCaseId());
    ProcessorKey key = ProcessorKey.buildKey(rmRequest);
    List<InboundProcessor<FwmtActionInstruction>> processors = pauseProcessorMap.get(key).stream().filter(p -> p.isValid(rmRequest, cache)).collect(Collectors.toList());
    if (processors.size()==0){
      //TODO throw routing error & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Could not find a PAUSE processor for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Could not find a PAUSE processor for request from RM", rmRequest, cache);
    }
    if (processors.size()>1){
      //TODO throw routing error  & exit;
      eventManager.triggerErrorEvent(this.getClass(), "Found multiple PAUSE processors for request from RM", String.valueOf(rmRequest.getCaseId()), ROUTING_FAILED);
      throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED,  "Found multiple PAUSE processors for request from RM", rmRequest, cache);
    }
    processors.get(0).process(rmRequest, cache);
  }

  /*private void routingFailure()
  String ffuDetail = ffu.toRoutingString();
  String cacheDetail = (cache == null) ? "null" : cache.toRoutingString();
  String msg = this.getClass().getSimpleName() + " is unable to route the following message: " +
      ffuDetail + " with " + cacheDetail;
  eventManager.triggerErrorEvent(this.getClass(), msg, String.valueOf(ffu.getCaseId()), ROUTING_FAILED);
  throw new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
*/

/*  public void process(FwmtActionInstruction ffu) throws GatewayException {
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

  */
}
