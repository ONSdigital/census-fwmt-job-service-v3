package uk.gov.ons.census.fwmt.jobservice.comet.converter.spg;

import java.util.List;
import java.util.Objects;

import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.TypeEnum;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.error.GatewayException.Fault;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;

public final class SpgMapper {

  public static CaseRequest map(FieldworkFollowup in) throws GatewayException {
    try {
    CaseRequest.CaseRequestBuilder out = CaseRequest.builder();

    out.reference(in.getCaseRef());
    out.type(TypeEnum.CE);
    out.surveyType("SPG SITE");
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
      outAddress.uprn(Long.valueOf(in.getUprn()));
    outAddress.lines(List.of(
        in.getAddressLine1(),
        Objects.toString(in.getAddressLine2(), ""),
        Objects.toString(in.getAddressLine3(), "")
    ));
    outAddress.town(in.getTownName());
    outAddress.postcode(in.getPostcode());
    out.address(outAddress.build());

    Geography.GeographyBuilder outGeography = Geography.builder();
    outGeography.oa(in.getOa());
    outAddress.geography(outGeography.build());

    Location.LocationBuilder outLocation = Location.builder();
      outLocation.lat(Float.valueOf(in.getLatitude()));
      outLocation._long(Float.valueOf(in.getLongitude()));
    out.location(outLocation.build());

    out.uaa(in.getUaa());
    out.sai(false);

//    if (in.getAddressLevel().equals("U")) {
//      // SPG Unit
//      if (in.getHandDeliver()) {
//        // SPG Unit Deliver
//        out.surveyType("SPG Unit-D");
//      } else {
//        // SPG Unit Followup
//        out.surveyType("SPG Unit-F");
//      }
//    } else if (in.getAddressLevel().equals("E")) {
//      // SPG Site
//      if (in.getSecureEstablishment()) {
//        // SPG Secure Site
//        out.surveyType("SPG SECURE SITE");
//      } else {
//        // SPG Site
//        out.surveyType("SPG SITE");
//      }
//    } else {
//      // Unknown
//      // TODO proper error handling
//    }

    return out.build();

    } catch (NumberFormatException e) {
      throw new GatewayException(Fault.SYSTEM_ERROR, "Problem converting SPG Site Case", e);
    }

  }

}
