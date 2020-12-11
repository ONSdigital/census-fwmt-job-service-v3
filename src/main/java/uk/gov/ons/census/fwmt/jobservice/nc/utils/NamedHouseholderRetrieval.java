package uk.gov.ons.census.fwmt.jobservice.nc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.common.data.nc.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.common.data.nc.RefusalTypeDTO;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;

import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.UNABLE_TO_DECRYPT_NAME;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.UNABLE_TO_READ_EVENT_PAYLOAD;
import static uk.gov.ons.census.fwmt.jobservice.config.GatewayEventsConfig.DECRYPTED_HH_NAMES;


@Service
public class NamedHouseholderRetrieval {

  @Autowired
  private GatewayEventManager eventManager;

  @Value("${decryption.pgp}")
  private Resource privateKey;

  @Value("${decryption.password}")
  private String privateKeyPassword;

  public String getAndSortRmRefusalCases(String caseId, CaseDetailsDTO houseHolder) throws GatewayException {
    StringBuilder contact = new StringBuilder();
    boolean isHardRefusal = false;

    if (houseHolder != null && houseHolder.getRefusalReceived() != null){
      isHardRefusal = houseHolder.getRefusalReceived().equals(RefusalTypeDTO.HARD_REFUSAL);
    }

    if (isHardRefusal) {
      List<CaseDetailsEventDTO> caseEventDetails;
      caseEventDetails = houseHolder.getEvents();

      for (int i = 0; i < caseEventDetails.size(); i++) {
        caseEventDetails.sort((date1, date2) -> {
          OffsetDateTime firstDate;
          OffsetDateTime secondDate;

          firstDate = date1.getEventDate();
          secondDate = date2.getEventDate();

          return firstDate.compareTo(secondDate);
        });

        for (CaseDetailsEventDTO individualCaseDetail : caseEventDetails) {
          String eventPayload = individualCaseDetail.getEventPayload() != null
              ? individualCaseDetail.getEventPayload() : "";
          String refusalEvent = individualCaseDetail.getEventType() != null ? individualCaseDetail.getEventType() : "";
          if (refusalEvent.equals(RefusalTypeDTO.HARD_REFUSAL.toString()) && !eventPayload.equals("") ) {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode collectionCase;
            JsonNode householdContact;
            try {
              collectionCase = objectMapper.readTree(eventPayload.substring(1, eventPayload.length() -1));
              householdContact = collectionCase.get("contact");

            } catch (JsonProcessingException e) {
              eventManager.triggerErrorEvent(this.getClass(), "Unable to read eventPayload", String.valueOf(caseId), UNABLE_TO_READ_EVENT_PAYLOAD,
                  "eventPayload", eventPayload);
              throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Unable to read eventPayload");
            }

            if (householdContact != null) {
              String decryptedFirstname;
              String decryptedSurname;
              String decryptedTitle;
              String isHouseHolder;

              try {
                decryptedTitle = !householdContact.get("title").toString().equals("") ? DecryptNames.decryptFile(
                    privateKey.getInputStream(), householdContact.get("title").toString(),
                    privateKeyPassword.toCharArray()) : "";

                decryptedFirstname = !householdContact.get("forename").toString().equals("") ? DecryptNames.decryptFile(
                    privateKey.getInputStream(), householdContact.get("forename").toString(),
                    privateKeyPassword.toCharArray()) : "";

                decryptedSurname = !householdContact.get("surname").toString().equals("") ? DecryptNames.decryptFile(
                    privateKey.getInputStream(), householdContact.get("surname").toString(),
                    privateKeyPassword.toCharArray()) : "";
              } catch (IOException | NullPointerException e){
                eventManager.triggerErrorEvent(this.getClass(), "Unable to decrypt householder name", String.valueOf(caseId), UNABLE_TO_DECRYPT_NAME);
                throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Unable to decrypt the householder name");
              }

              isHouseHolder =  collectionCase.get("isHouseholder") != null && collectionCase.get("isHouseholder").toString().equals("true") ? "Yes" : "No";

              if (decryptedSurname != null) {
                contact.insert(0, "Name =");
                if (decryptedTitle != null) {
                  contact.append(" ").append(decryptedTitle);
                }
                if (decryptedFirstname != null) {
                  contact.append(" ").append(decryptedFirstname);
                }
                contact.append(" ").append(decryptedSurname).append("\n");
                // Do we need a placeholder in the description to say that this is the name of the householder?
                // I've added one temporarily
                contact.append("Named householder = ").append(isHouseHolder);
                eventManager.triggerEvent(caseId,DECRYPTED_HH_NAMES);
                break;
              }
            }
          }
        }
      }
    } else {
      return "";
    }
    return contact.toString();
  }
}
