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

@Service
public class NamedHouseholderRetrieval {

  @Autowired
  private GatewayEventManager eventManager;

  @Value("${decryption.pgp}")
  private Resource testPrivateKey;

  @Value("${decryption.password}")
  private String testPassword;

  public String getAndSortRmRefusalCases(String caseId, CaseDetailsDTO houseHolder) throws GatewayException {
    StringBuilder contact = new StringBuilder();

    if (houseHolder.getRefusalReceived() != null && houseHolder.getRefusalReceived().equals(RefusalTypeDTO.HARD_REFUSAL)) {
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
              String decryptedFirstname = null;
              String decryptedSurname = null;
              String isHouseHolder;

              if (!householdContact.get("forename").toString().equals("")) {
                try {
                  decryptedFirstname = DecryptNames.decryptFile(testPrivateKey.getInputStream(), householdContact.get("forename").toString(),
                      testPassword.toCharArray());
                } catch (IOException e){
                  eventManager.triggerErrorEvent(this.getClass(), "Unable to decrypt householder forename", String.valueOf(caseId), UNABLE_TO_DECRYPT_NAME,
                      "forename", householdContact.get("forename").toString());
                  throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Unable to decrypt the householders forename");
                }
              }
              if (!householdContact.get("surname").toString().equals("")) {
                try{
                  decryptedSurname = DecryptNames.decryptFile(testPrivateKey.getInputStream(), householdContact.get("surname").toString(),
                      testPassword.toCharArray());
                } catch (IOException e){
                  eventManager.triggerErrorEvent(this.getClass(), "Unable to decrypt householder surname", String.valueOf(caseId), UNABLE_TO_DECRYPT_NAME,
                      "forename", householdContact.get("surname").toString());
                  throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, e, "Unable to decrypt the householders surname");
                }
              }
              if (collectionCase.get("isHouseholder").toString().equals("true")) {
                isHouseHolder = "Yes";
              } else {
                isHouseHolder = "No";
              }

              if (decryptedSurname != null) {
                contact.append(" " + decryptedSurname + " ");
                if (decryptedFirstname != null) {
                  contact.insert(0, " " + decryptedFirstname);
                }
                // Do we need a placeholder in the description to say that this is the name of the householder?
                // I've added one temporarily
                contact.insert(0, "Householder name = ");
                contact.append("Named householder = ").append(isHouseHolder);
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
