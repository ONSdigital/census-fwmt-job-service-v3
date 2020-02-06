package uk.gov.ons.census.fwmt.jobservice.service.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.jobservice.dto.rm.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.Address;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.CaseType;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.Contact;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.Geography;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.Location;
import uk.gov.ons.census.fwmt.jobservice.dto.tm.PutCaseRequest;

import java.util.List;

@Slf4j
@Service
public class SpgMapper {

  public SpgMapper() {
  }

  public PutCaseRequest map(FieldworkFollowup in) {
    PutCaseRequest.PutCaseRequestBuilder out = PutCaseRequest.builder();

    out.reference(in.getCaseRef());
    out.type(CaseType.CE);
    out.category("Not applicable");
    out.estabType(in.getEstabType());
    out.requiredOfficer(in.getFieldOfficerId());
    out.coordCode(in.getFieldCoordinatorId());

    Contact.ContactBuilder outContact = Contact.builder();
    outContact.organisationName(in.getOrganisationName());
    outContact.name(in.getForename() + " " + in.getSurname());
    outContact.phone(in.getPhoneNumber());
    out.contact(outContact.build());

    Address.AddressBuilder outAddress = Address.builder();
    try {
      outAddress.uprn(Long.valueOf(in.getUprn()));
    } catch (NumberFormatException e) {
      // TODO proper error handling
    }
    outAddress.lines(List.of(
        in.getAddressLine1(),
        in.getAddressLine2(),
        in.getAddressLine3()
    ));
    outAddress.town(in.getTownName());
    outAddress.postcode(in.getPostcode());
    out.address(outAddress.build());

    Geography.GeographyBuilder outGeography = Geography.builder();
    outGeography.oa(in.getOa());
    outAddress.geography(outGeography.build());

    Location.LocationBuilder outLocation = Location.builder();
    try {
      outLocation.lat(Float.valueOf(in.getLatitude()));
      outLocation._long(Float.valueOf(in.getLongitude()));
    } catch (NumberFormatException e) {
      // TODO proper error handling
    }
    out.location(outLocation.build());

    out.uaa(in.getUaa());
    out.sai(false);

    if (in.getAddressLevel().equals("U")) {
      // SPG Unit
      if (in.getHandDeliver()) {
        // SPG Unit Deliver
        out.surveyType("SPG Unit-D");
      } else {
        // SPG Unit Followup
        out.surveyType("SPG Unit-F");
      }
    } else if (in.getAddressLevel().equals("E")) {
      // SPG Site
      if (in.getSecureEstablishment()) {
        // SPG Secure Site
        out.surveyType("SPG SECURE SITE");
      } else {
        // SPG Site
        out.surveyType("SPG SITE");
      }
    } else {
      // Unknown
      // TODO proper error handling
    }

    return out.build();
  }

}
