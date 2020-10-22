package uk.gov.ons.census.fwmt.jobservice.service.converter.ccs;

import uk.gov.ons.census.fwmt.common.data.tm.Address;
import uk.gov.ons.census.fwmt.common.data.tm.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.tm.CaseType;
import uk.gov.ons.census.fwmt.common.data.tm.Geography;
import uk.gov.ons.census.fwmt.common.data.tm.SurveyType;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.common.CommonCreateConverter;

import java.util.ArrayList;
import java.util.List;

public class CcsPropertyListingCreateConverter {

  private CcsPropertyListingCreateConverter() {
  }

  public static CaseRequest.CaseRequestBuilder convertCcs(
      FwmtActionInstruction ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder builder) {
    CaseRequest.CaseRequestBuilder commonBuilder = CommonCreateConverter.convertCommon(ffu, cache, builder);

    commonBuilder.type((ffu.getAddressType()!=null)?CaseType.valueOf(ffu.getAddressType()):CaseType.CCS);
    commonBuilder.surveyType((ffu.getSurveyType()!=null)?ffu.getSurveyType():SurveyType.CCS_PL);
    commonBuilder.category("Not applicable");

    commonBuilder.estabType((ffu.getEstabType()!=null)?ffu.getEstabType():"HH");
    commonBuilder.coordCode(ffu.getFieldCoordinatorId());
    commonBuilder.requiredOfficer(ffu.getFieldOfficerId());

    Geography outGeography = Geography.builder().oa(ffu.getOa()).build();


    List<String> addressLines = new ArrayList<>();
    addressLines.add(ffu.getPostcode());

    Address outAddress = Address.builder()
        .lines(addressLines)
        .postcode(ffu.getPostcode())
        .geography(outGeography)
        .build();
    commonBuilder.address(outAddress);

    return commonBuilder;
  }

  public static CaseRequest convertCcsPropertyListing(FwmtActionInstruction ffu, GatewayCache cache) {
    return CcsPropertyListingCreateConverter
        .convertCcs(ffu, cache, CaseRequest.builder())
        .build();
  }
}