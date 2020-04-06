package uk.gov.ons.census.fwmt.jobservice.service.routing.spg.update;

import java.util.List;

import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.routing.RouterList;
import uk.gov.ons.census.fwmt.jobservice.service.routing.spg.SpgRouter;

@Component
public class SpgUpdateRouterList extends RouterList<SpgUpdateRouter> implements SpgRouter{

  public SpgUpdateRouterList(GatewayEventManager eventManager, List<SpgUpdateRouter> routers) {
    super(eventManager);
    setRouters(routers);
  }


//  @Override
//  public Void routeUnsafe(FieldworkFollowup ffu, GatewayCache cache) throws GatewayException {
//    CaseReopenCreateRequest request = router.route(ffu, cache, eventManager);
//
//    eventManager.triggerEvent(String.valueOf(ffu.getCaseId()), COMET_UPDATE_SENT, "Case Ref", ffu.getCaseRef());
//
//    ResponseEntity<Void> response = cometRestClient.sendReopen(request, ffu.getCaseId());
//
//    routingValidator.validateResponseCode(response, ffu.getCaseId(), "Update", FAILED_TO_UPDATE_TM_JOB);
//
//    eventManager
//        .triggerEvent(String.valueOf(ffu.getCaseId()), COMET_UPDATE_ACK, "Case Ref", ffu.getCaseRef(), "Response Code",
//            response.getStatusCode().name());
//
//    return null;
//  }

  @Override
  public boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    try {
      // relies on the validation of: SpgRouter
      return ffu.getActionInstruction().equals("UPDATE")
          && ffu.getSurveyName().equals("CENSUS")
          && ffu.getAddressType().equals("SPG");
    } catch (NullPointerException e) {
      return false;
    }
  }
}
