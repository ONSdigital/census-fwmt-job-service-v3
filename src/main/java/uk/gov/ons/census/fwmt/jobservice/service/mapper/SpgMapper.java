package uk.gov.ons.census.fwmt.jobservice.service.mapper;

import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.jobservice.dto.rm.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.PutCase;

import java.util.List;

public final class SpgMapper {

  private SpgMapper() {
  }

  public static PutCase map(FieldworkFollowup in) {
    PutCase out = new PutCase();

    out.setReference(in.getCaseRef());
    out.setType(PutCase.TypeEnum.CE);
    out.setCategory("Not applicable");
    out.setEstabType(in.getEstabType());
    out.setRequiredOfficer(in.getFieldOfficerId());
    out.setCoordCode(in.getFieldCoordinatorId());

    Contact outContact = new Contact();
    out.setContact(outContact);
    outContact.setOrganisationName(in.getOrganisationName());
    outContact.setName(in.getForename() + " " + in.getSurname());
    outContact.setPhone(in.getPhoneNumber());

    Address outAddress = new Address();
    out.setAddress(outAddress);
    try {
      outAddress.setUprn(Long.valueOf(in.getUprn()));
    } catch (NumberFormatException e) {
      // TODO proper error handling
    }
    outAddress.setLines(List.of(
        in.getAddressLine1(),
        in.getAddressLine2(),
        in.getAddressLine3()
    ));
    outAddress.setTown(in.getTownName());
    outAddress.setPostcode(in.getPostcode());

    Geography outGeography = new Geography();
    outAddress.setGeography(outGeography);
    outGeography.setOa(in.getOa());

    Location outLocation = new Location();
    out.setLocation(outLocation);
    try {
      outLocation.setLat(Float.valueOf(in.getLatitude()));
      outLocation.set_long(Float.valueOf(in.getLongitude()));
    } catch (NumberFormatException e) {
      // TODO proper error handling
    }

    out.setUaa(in.getUaa());
    out.setSai(false);

    if (in.getAddressLevel().equals("U")) {
      // SPG Unit
      if (in.getHandDeliver()) {
        // SPG Unit Deliver
        out.setSurveyType("SPG Unit-D");
      } else {
        // SPG Unit Followup
        out.setSurveyType("SPG Unit-F");
      }
    } else if (in.getAddressLevel().equals("E")) {
      // SPG Site
      if (in.getSecureEstablishment()) {
        // SPG Secure Site
        out.setSurveyType("SPG SECURE SITE");
      } else {
        // SPG Site
        out.setSurveyType("SPG SITE");
      }
    } else {
      // Unknown
      // TODO proper error handling
    }

    return out;
  }

}
