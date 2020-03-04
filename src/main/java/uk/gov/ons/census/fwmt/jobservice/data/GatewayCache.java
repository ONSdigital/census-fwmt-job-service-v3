package uk.gov.ons.census.fwmt.jobservice.data;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GatewayCache {
  public final String caseId;
  public final boolean existsInFwmt;
  public final boolean isDelivered;
  public final String preferredName;
  public final String address1;
  public final String address2;
  public final String careCode;
  public final String accessInfo;
  public final String managerTitle;
  public final String managerFirstname;
  public final String managerSurname;
  public final String contactPhoneNumber;
}
