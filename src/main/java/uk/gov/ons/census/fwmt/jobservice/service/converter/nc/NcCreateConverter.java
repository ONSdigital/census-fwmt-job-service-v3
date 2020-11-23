package uk.gov.ons.census.fwmt.jobservice.service.converter.nc;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.http.rm.RmRestClient;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class NcCreateConverter {

  @Autowired
  private RmRestClient rmRestClient;

  private NcCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertNC(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.reference("NC" + ffu.getCaseRef());
    commonBuilder.type(CaseType.NC);
    commonBuilder.surveyType(SurveyType.NC);
    commonBuilder.category("HH");

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

  public static CaseRequest convertNcEnglandAndWales(FwmtActionInstruction ffu, GatewayCache cache) {
    return NcCreateConverter
        .convertNC(ffu, cache, CaseRequest.builder())
        .sai("Sheltered Accommodation".equals(ffu.getEstabType()))
        .specialInstructions(getSpecialInstructions(cache))
        .description(getDescription(ffu, cache))
        .build();
  }

  private static String getDescription(FwmtActionInstruction ffu, GatewayCache cache) {
    StringBuilder description = new StringBuilder();
    if (cache != null && cache.getCareCodes() != null && !cache.getCareCodes().isEmpty()) {
      description.append(cache.getCareCodes());
      description.append("\n");
    }
    if (ffu.getAddressType().equals(CaseType.HH.toString())) {
      String householderDetails = new NcCreateConverter().getHouseholderDetails(ffu.getCaseId());
      if (!householderDetails.isEmpty()) {
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

  private String getHouseholderDetails(String caseId) {
    String nameJson = "";
    try {
      nameJson = getAndSortRmRefusalCases(caseId);
    } catch (GatewayException e) {

    }
    return nameJson;
  }

  private String getAndSortRmRefusalCases(String caseID) throws GatewayException {
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode caseEvents = null;
    JSONArray recordsArray = null;
    JSONArray sortedJsonArray = null;

    String contact = "";

    try {
      caseEvents = objectMapper.readTree(rmRestClient.getCase(caseID).toString());
    } catch (JsonProcessingException e) {

    }

    if (caseEvents != null &&
        (caseEvents.get("refusalReceived") != null && caseEvents.get("refusalReceived").asText()
            .equals("HARD_REFUSAL"))) {
      recordsArray = new JSONArray(caseEvents.get("events"));

      List<JSONObject> refusalsForCase = new ArrayList<>();
      for (int i = 0; i < recordsArray.length(); i++) {

        if (recordsArray.getJSONObject(i).get("eventType") == "HARD_REFUSAL") {
          refusalsForCase.add(recordsArray.getJSONObject(i));
        }
      }

      refusalsForCase.sort(new Comparator<>() {
        private static final String dateNode = "createdDateTime";

        @Override
        public int compare(JSONObject dateTime1, JSONObject dateTime2) {
          String firstDate = "";
          String secondDate = "";
          try {
            firstDate = (String) dateTime1.get(dateNode);
            secondDate = (String) dateTime2.get(dateNode);
          } catch (JSONException e) {

          }
          return firstDate.compareTo(secondDate);
        }
      });

      for (int i = 0; i < recordsArray.length(); i++) {
        JSONArray eventPayload = null;
        String getContact;

        if (refusalsForCase.get(i).get("eventPayload") != null){
          eventPayload.put(refusalsForCase.get(i).get("eventPayload"));
        }

      }
    }
    return "";
  }
}
