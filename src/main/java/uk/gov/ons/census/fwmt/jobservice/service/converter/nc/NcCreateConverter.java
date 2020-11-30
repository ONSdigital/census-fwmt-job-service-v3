package uk.gov.ons.census.fwmt.jobservice.service.converter.nc;

import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.events.component.GatewayEventManager;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.nc.utils.NamedHouseholdDetails;
import uk.gov.ons.census.fwmt.jobservice.refusal.dto.CaseDetailsDTO;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.List;
import java.util.Objects;

public class NcCreateConverter {

  private NcCreateConverter() {
  }

  @Autowired
  private GatewayEventManager eventManager;

  @Autowired
  private RmRestClient rmRestClient;

  public static final String UNABLE_TO_DECRYPT_RM_API_RESPONSE = "UNABLE_TO_DECRYPT_RM_API_RESPONSE";

  public static CaseRequest.CaseRequestBuilder convertNC(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.reference("NC" + ffu.getCaseRef());
    commonBuilder.type(CaseType.NC);
    commonBuilder.surveyType(SurveyType.NC);
    commonBuilder.category("HH");
    commonBuilder.requiredOfficer(ffu.getFieldOfficerId());

    Geography outGeography = Geography
        .builder()
        .oa(ffu.getOa())
        .build();

    Address outAddress = Address.builder()
        .lines(List.of(
            ffu.getAddressLine1(),
            Objects.toString(ffu.getAddressLine2(), ""),
            Objects.toString(ffu.getAddressLine3(), "")
        ))
        .town(ffu.getTownName())
        .postcode(ffu.getPostcode())
        .geography(outGeography)
        .build();

    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertNcEnglandAndWales(FwmtActionInstruction ffu, GatewayCache cache, CaseDetailsDTO householder)
      throws GatewayException {
    return NcCreateConverter
        .convertNC(ffu, cache, CaseRequest.builder())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .specialInstructions(getSpecialInstructions(cache))
        .description(getDescription(ffu, cache, householder))
        .build();
  }

  private static String getDescription(FwmtActionInstruction ffu, GatewayCache cache, CaseDetailsDTO householder) throws GatewayException {
    StringBuilder description = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      description.append(cache.getCareCodes());
      description.append("\n");
    }
    if (ffu.getAddressType().equals(CaseType.HH.toString()) && householder != null) {
      String householderDetails = new NcCreateConverter().getHouseholderDetails(ffu.getCaseId(), householder);
      if (householderDetails != null && !householderDetails.equals("")) {
        description.append(householderDetails);
        description.append("\n");
      }
    }
    return description.toString();
  }

  private static String getSpecialInstructions(GatewayCache cache) {
    StringBuilder instruction = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      instruction.append(cache.getCareCodes());
      instruction.append("\n");
    }
    if (cache != null && cache.getAccessInfo() != null && !cache.getAccessInfo().isEmpty()) {
      instruction.append(cache.getAccessInfo());
      instruction.append("\n");
    }
    return instruction.toString();
  }

  private String getHouseholderDetails(String caseId, CaseDetailsDTO houseHolder) throws GatewayException {
    NamedHouseholdDetails namedHouseholdDetails = new NamedHouseholdDetails();
    String houseHolderDetails = "";
    try {
      houseHolderDetails = namedHouseholdDetails.getAndSortRmRefusalCases(caseId, houseHolder);
    } catch (GatewayException | NullPointerException e) {
      eventManager.triggerErrorEvent(this.getClass(), "Unable to decrypt householder details from RM Case API response",
          caseId, UNABLE_TO_DECRYPT_RM_API_RESPONSE);
      throw new GatewayException(GatewayException.Fault.BAD_REQUEST,
          "Unable to decrypt householder details from RM Case API response", caseId);
    }
    return houseHolderDetails;
  }

}

