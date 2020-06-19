package uk.gov.ons.census.fwmt.jobservice.service.converter.ce;

import uk.gov.ons.census.fwmt.common.data.tm.CeCaseExtension;
import uk.gov.ons.census.fwmt.common.data.tm.ReopenCaseRequest;
import uk.gov.ons.census.fwmt.common.rm.dto.FwmtActionInstruction;

public final class CeUpdateConverter {

  private CeUpdateConverter() {
  }

  private static ReopenCaseRequest.ReopenCaseRequestBuilder convertCommon(FwmtActionInstruction ffu,
      ReopenCaseRequest.ReopenCaseRequestBuilder builder, String surveyType) {

    int actualResponse = 0;
    int expectedResponse = 0;

    builder.id(ffu.getCaseId());

    if (surveyType.equals("unit") || surveyType.equals("estab")  ) {
      actualResponse = ffu.getCeActualResponses();
      expectedResponse = ffu.getCeExpectedCapacity();
    }

    if (surveyType.equals("unit")) {
      CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
          .expectedResponses(expectedResponse)
          .actualResponses(actualResponse)
          .build();
      builder.ce(ceCaseExtension);
    } else {
      CeCaseExtension ceCaseExtension = CeCaseExtension.builder()
          .ce1Complete(ffu.isCe1Complete())
          .expectedResponses(expectedResponse)
          .actualResponses(actualResponse)
          .build();
      builder.ce(ceCaseExtension);
    }
    return builder;
  }

  public static ReopenCaseRequest convertEstab(FwmtActionInstruction ffu) {
    return CeUpdateConverter.convertCommon(ffu, ReopenCaseRequest.builder(), "estab")
        .build();
  }

  public static ReopenCaseRequest convertSite(FwmtActionInstruction ffu) {
    return CeUpdateConverter.convertCommon(ffu, ReopenCaseRequest.builder(), "site")
        .build();
  }

  public static ReopenCaseRequest convertUnit(FwmtActionInstruction ffu) {
    return CeUpdateConverter.convertCommon(ffu, ReopenCaseRequest.builder(), "unit")
        .build();
  }
}

