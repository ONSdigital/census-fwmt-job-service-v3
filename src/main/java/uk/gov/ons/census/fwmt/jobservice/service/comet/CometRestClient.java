package uk.gov.ons.census.fwmt.jobservice.service.comet;

import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import com.microsoft.aad.adal4j.ClientCredential;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePauseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
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

  // O365 credentials for authentication w/o login prompt

  // Azure Directory OAUTH 2.0 AUTHORIZATION ENDPOINT
  private final String authority;
  private final String resource;
  private final String cometURL;
  private final String clientID;
  private final String clientSecret;

  // temporary store for authentication result
  private AuthenticationResult auth;

  public CometRestClient(
      @Qualifier("TM") RestTemplate restTemplate,
      GatewayEventManager gatewayEventManager,
      @Value("${totalmobile.baseUrl}") String baseUrl,
      @Value("${totalmobile.operation.case.create.path}") String tmPath,
      @Value("${totalmobile.comet.clientID}") String clientID,
      @Value("${totalmobile.comet.clientSecret}") String clientSecret,
      @Value("${totalmobile.comet.resource}") String resource,
      @Value("${totalmobile.comet.authority}") String authority) {
    this.restTemplate = restTemplate;
    this.gatewayEventManager = gatewayEventManager;
    this.cometURL = baseUrl + tmPath;
    this.clientID = clientID;
    this.clientSecret = clientSecret;
    this.resource = resource;
    this.authority = authority;
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
      AuthenticationContext context = new AuthenticationContext(authority, false, service);
      ClientCredential cc = new ClientCredential(clientID, clientSecret);

      Future<AuthenticationResult> future = context.acquireToken(resource, cc, null);
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
    if ((!isAuthed() || isExpired()) && !clientID.isEmpty() && !clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed())
      httpHeaders.setBearerAuth(auth.getAccessToken());
    if (caseRequest.getClass().equals(CasePauseRequest.class)) {
      basePathway = basePathway + "/pause";
    }

    HttpEntity<?> body = new HttpEntity<>(caseRequest, httpHeaders);
    return restTemplate.exchange(basePathway, HttpMethod.PUT, body, Void.class);
  }

  public ModelCase getCase(String caseId) throws GatewayException {
    String basePathway = cometURL + caseId;
    if ((!isAuthed() || isExpired()) && !clientID.isEmpty() && !clientSecret.isEmpty())
      auth();
    HttpHeaders httpHeaders = new HttpHeaders();
    if (isAuthed())
      httpHeaders.setBearerAuth(auth.getAccessToken());

    HttpEntity<?> body = new HttpEntity<>(httpHeaders);
    ResponseEntity<ModelCase> request = restTemplate.exchange(basePathway, HttpMethod.GET, body, ModelCase.class);

    return request.getBody();
  }
}
