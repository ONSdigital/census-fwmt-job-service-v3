package uk.gov.ons.census.fwmt.jobservice.data;

public enum CaseType {
  CANCEL("Cancel"),
  CREATE("Create"),
  UPDATE("Update");

  public final String name;

  CaseType(String name) {
    this.name = name;
  }

  @Override public String toString() {
    return name;
  }
}
