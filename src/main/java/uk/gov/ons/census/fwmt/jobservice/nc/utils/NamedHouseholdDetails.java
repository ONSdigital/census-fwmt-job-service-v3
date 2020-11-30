package uk.gov.ons.census.fwmt.jobservice.nc.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.CaseDetailsEventDTO;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.CollectionCaseDTO;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.RefusalTypeDTO;
import uk.gov.ons.census.fwmt.jobservice.transition.utils.MessageConverter;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;
import java.time.OffsetDateTime;
import java.util.List;

public final class NamedHouseholdDetails {

  public NamedHouseholdDetails() {}

  @Autowired
  private GatewayEventManager eventManager;

  @Value("${decryption.pgp.privateKey}")
  private Resource testPrivateKey;

  @Value("${decryption.password}")
  private String testPassword;

  public static final String UNABLE_TO_READ_RM_API_RESPONSE = "UNABLE_TO_READ_RM_API_RESPONSE";

  public String getAndSortRmRefusalCases(String caseId, CaseDetailsDTO houseHolder) throws GatewayException {
    MessageConverter messageConverter = new MessageConverter();
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
            CollectionCaseDTO collectionCaseDTO = messageConverter.convertMessageToDTO(CollectionCaseDTO.class,
                eventPayload);

            if (collectionCaseDTO != null && collectionCaseDTO.getIsHouseholder() != null) {
              ByteArrayInputStream privateKeys = new ByteArrayInputStream(testPrivateKey.toString().getBytes(Charset.defaultCharset()));
              String decryptedFirstname = null;
              String decryptedSurname = null;
              String isHouseHolder;

              if (collectionCaseDTO.getForeName() != null && !collectionCaseDTO.getForeName().equals("")) {
                decryptedFirstname = DecryptNames.decryptFile(privateKeys, collectionCaseDTO.getForeName(),
                    testPassword.toCharArray());
              }
              if (collectionCaseDTO.getSurname() != null && !collectionCaseDTO.getSurname().equals("")) {
                decryptedSurname = DecryptNames.decryptFile(privateKeys, collectionCaseDTO.getSurname(),
                    testPassword.toCharArray());
              }
              if (collectionCaseDTO.getIsHouseholder().equals("true")) {
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
