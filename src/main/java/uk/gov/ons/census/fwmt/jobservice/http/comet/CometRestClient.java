package uk.gov.ons.census.fwmt.jobservice.http.comet;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseReopenCreateRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.config.CometConfig;
import uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig;

import java.net.MalformedURLException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Component
public class CometRestClient {

  private final RestTemplate restTemplate;
  private final GatewayEventManager gatewayEventManager;

  // cached endpoint, to avoid repeated string concats of baseUrl + caseCreatePath
  private final String cometURL;

  private final CometConfig cometConfig;

  // temporary store for authentication result
  private AuthenticationResult auth;

  public CometRestClient(
      CometConfig cometConfig,
      RestTemplateBuilder restTemplateBuilder,
      GatewayEventManager gatewayEventManager) {
    this.cometConfig = cometConfig;
    this.restTemplate = restTemplateBuilder.errorHandler(new CometRestClientResponseErrorHandler())
        .basicAuthentication(cometConfig.userName, cometConfig.password).build();
    this.gatewayEventManager = gatewayEventManager;
    this.cometURL = cometConfig.baseUrl + cometConfig.caseCreatePath;
    this.auth = null;
  }

  private boolean isAuthed() {
    return this.auth != null;
  }

  private boolean isExpired() {
    return !auth.getExpiresOnDate().after(new Date());
  }

  private void auth() throws GatewayException {
    ExecutorService service = Executors.newFixedThreadPool(1);
    try {
      AuthenticationContext context = new AuthenticationContext(cometConfig.authority, false, service);
      ClientCredential cc = new ClientCredential(cometConfig.clientId, cometConfig.clientSecret);

      Future<AuthenticationResult> future = context.acquireToken(cometConfig.resource, cc, null);
      this.auth = future.get();
    } catch (MalformedURLException | InterruptedException | ExecutionException e) {
      String errorMsg = "Failed to Authenticate with Totalmobile";
      gatewayEventManager
          .triggerErrorEvent(this.getClass(), errorMsg, "<N/A_CASE_ID>", GatewayEventsConfig.FAILED_TM_AUTHENTICATION);
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, errorMsg, e);
    } finally {
      service.shutdown();
    }
  }

  public <A> ResponseEntity<Void> sendRequest(A caseRequest, String caseId) throws GatewayException {
    String basePathway = cometURL + caseId;
    if ((!isAuthed() || isExpired()) && !cometConfig.clientId.isEmpty() && !cometConfig.clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed()) {
      httpHeaders.setBearerAuth(auth.getAccessToken());
    }

    if (caseRequest instanceof CaseCreateRequest) {
      HttpEntity<?> body = new HttpEntity<>(caseRequest, httpHeaders);
      return restTemplate.exchange(basePathway, HttpMethod.PUT, body, Void.class);

    } else if (caseRequest instanceof CasePauseRequest) {
      HttpEntity<?> body = new HttpEntity<>(caseRequest, httpHeaders);
      return restTemplate.exchange(basePathway + "/pause", HttpMethod.PUT, body, Void.class);

    } else if (caseRequest instanceof CaseReopenCreateRequest) {
      HttpEntity<?> body = new HttpEntity<>(caseRequest, httpHeaders);
      return restTemplate.exchange(basePathway + "/reopen", HttpMethod.POST, body, Void.class);

    } else {
      return null;
    }
  }

  public ModelCase getCase(String caseId) throws GatewayException {
    String basePathway = cometURL + caseId;
    if ((!isAuthed() || isExpired()) && !cometConfig.clientId.isEmpty() && !cometConfig.clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed())
      httpHeaders.setBearerAuth(auth.getAccessToken());

    HttpEntity<?> body = new HttpEntity<>(httpHeaders);
    ResponseEntity<ModelCase> request = restTemplate.exchange(basePathway, HttpMethod.GET, body, ModelCase.class);

    return request.getBody();
  }
}
