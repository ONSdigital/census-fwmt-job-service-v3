package uk.gov.ons.census.fwmt.jobservice.nc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.census.ffa.storage.utils.StorageUtils;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventHardRefusal;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalContact;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.DECRYPTED_HH_NAMES;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.UNABLE_TO_READ_EVENT_PAYLOAD;

@Service
public class NamedHouseholderRetrieval {

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private StorageUtils storageUtils;

  @Value("${decryption.pgp}")
  private String privateKey;

  @Value("${decryption.password}")
  private String privateKeyPassword;

  @Autowired
  private ObjectMapper objectMapper;

  public String getAndSortRmRefusalCases(String caseId, CaseDetailsDTO houseHolder) throws GatewayException {
    URI privateKeyUri = URI.create(privateKey);
    StringBuilder contact = new StringBuilder();
    OffsetDateTime previousDate = null;

    List<CaseDetailsEventDTO> caseEventDetails;
    caseEventDetails = houseHolder.getEvents();

    CaseDetailsEventDTO currentRefusal = new CaseDetailsEventDTO();

    for (CaseDetailsEventDTO checkForRefusal : caseEventDetails) {
      OffsetDateTime currentDate = checkForRefusal.getEventDate();
      boolean isRefusal = checkForRefusal.getEventType().equals("REFUSAL_RECEIVED");
      if (isRefusal && (previousDate == null || currentDate.compareTo(previousDate) > 0)) {
        currentRefusal = checkForRefusal;
        previousDate = checkForRefusal.getEventDate();
      }
    }

    CaseDetailsEventHardRefusal householdContact;

    try {
      householdContact = objectMapper.readValue(currentRefusal.getEventPayload(), CaseDetailsEventHardRefusal.class);
    } catch (JsonProcessingException e) {
      eventManager.triggerErrorEvent(this.getClass(), "Unable to read eventPayload", String.valueOf(caseId), UNABLE_TO_READ_EVENT_PAYLOAD,
          "eventPayload", currentRefusal.getEventPayload());
      throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Unable to read eventPayload");
    }

    RefusalContact refusalContact = householdContact.getContact();

    String decryptedFirstname;
    String decryptedSurname;
    String decryptedTitle;
    String isHouseHolder;

    decryptedTitle = refusalContact.getTitle() != null && !refusalContact.getTitle().equals("") ? DecryptNames.decryptFile(
        storageUtils.getFileInputStream(privateKeyUri), refusalContact.getTitle(),
        privateKeyPassword.toCharArray()) : "";

    decryptedFirstname = refusalContact.getForename() != null && !refusalContact.getForename().equals("") ? DecryptNames.decryptFile(
        storageUtils.getFileInputStream(privateKeyUri),  refusalContact.getForename(),
        privateKeyPassword.toCharArray()) : "";

    decryptedSurname = refusalContact.getSurname() != null && !refusalContact.getSurname().equals("") ? DecryptNames.decryptFile(
        storageUtils.getFileInputStream(privateKeyUri), refusalContact.getSurname(),
        privateKeyPassword.toCharArray()) : "";

    isHouseHolder = householdContact.getIsHouseholder().equals("true") ? "Yes" : "No";

    if (!decryptedSurname.equals("")) {
      contact.insert(0, "Name =");
      if (!decryptedTitle.equals("")) {
        contact.append(" ").append(decryptedTitle);
      }
      if (!decryptedFirstname.equals("")) {
        contact.append(" ").append(decryptedFirstname);
      }
      contact.append(" ").append(decryptedSurname).append("\n");
      contact.append("Named householder = ").append(isHouseHolder);
      eventManager.triggerEvent(caseId,DECRYPTED_HH_NAMES);
      return contact.toString();
    }
    return "";
  }
}
