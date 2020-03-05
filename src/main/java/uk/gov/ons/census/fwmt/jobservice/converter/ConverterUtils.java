package uk.gov.ons.census.fwmt.jobservice.converter;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.gatewaycache.GatewayCache;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

import java.util.List;
import java.util.Optional;

public class ConverterUtils {

  private ConverterUtils() {
  }

  public static Long parseLong(String input) throws GatewayException {
    if (input == null) {
      return null;
    } else {
      try {
        return Long.parseLong(input);
      } catch (NumberFormatException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Problem converting SPG Site Case", e);
      }
    }
  }

  public static Float parseFloat(String input) throws GatewayException {
    if (input == null) {
      return null;
    } else {
      try {
        return Float.parseFloat(input);
      } catch (NumberFormatException e) {
        throw new GatewayException(GatewayException.Fault.SYSTEM_ERROR, "Problem converting SPG Site Case", e);
      }
    }
  }

  public static Optional<CometConverter> maybeGetConverter(
      FieldworkFollowup ffu, GatewayCache cache, List<CometConverter> selectors) {
    return selectors.stream().filter(s -> s.isValid(ffu, cache)).findFirst();
  }

  public static CometConverter getConverter(FieldworkFollowup ffu, GatewayCache cache, List<CometConverter> selectors)
      throws GatewayException {
    return maybeGetConverter(ffu, cache, selectors).orElseThrow(() -> noConverter(ffu, cache));
  }

  private static GatewayException noConverter(FieldworkFollowup ffu, GatewayCache cache) {
    String ffMsg = "FieldworkFollowup(actionInstruction=%s, surveyName=%s, addressType=%s, addressLevel=%s, secureEstablishment=%s)";
    String gcMsg = "GatewayCache(caseId, existsInField, delivered)";
    String msg = "No converter found for " + ffMsg + " and " + gcMsg;
    return new GatewayException(GatewayException.Fault.VALIDATION_FAILED, msg, ffu, cache);
  }
}
