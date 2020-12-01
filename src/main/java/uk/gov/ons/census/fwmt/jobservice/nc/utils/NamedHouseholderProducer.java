package uk.gov.ons.census.fwmt.jobservice.nc.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.RefusalTypeDTO;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class NamedHouseholderProducer {

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
            JsonNode collectionCase = null;
            JsonNode householdContact = null;
            try {
              collectionCase = objectMapper.readTree(eventPayload.substring(1, eventPayload.length() -1));
              householdContact = collectionCase.get("contact");

            } catch (JsonProcessingException e) {

            }

            if (householdContact != null) {
              ByteArrayInputStream privateKeys = new ByteArrayInputStream(testPrivateKey.toString().getBytes(Charset.defaultCharset()));
              String decryptedFirstname = null;
              String decryptedSurname = null;
              String isHouseHolder;

              if (!householdContact.get("forename").toString().equals("")) {
                decryptedFirstname = DecryptNames.decryptFile(privateKeys, householdContact.get("forename").toString(),
                    testPassword.toCharArray());
              }
              if (!householdContact.get("surname").toString().equals("")) {
                decryptedSurname = DecryptNames.decryptFile(privateKeys, householdContact.get("surname").toString(),
                    testPassword.toCharArray());
              }
              if (collectionCase.get("isHouseholder").toString().equals("true")) {
                isHouseHolder = "Yes";
              } else {
                isHouseHolder = "No";
              }

              if (decryptedSurname != null) {
                contact.append(decryptedSurname);
                if (decryptedFirstname != null) {
                  contact.insert(0, decryptedFirstname);
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
