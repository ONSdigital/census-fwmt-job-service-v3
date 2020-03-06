package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import com.google.common.collect.Lists;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.ConverterUtils;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SpgUpdateCommon {

  private SpgUpdateCommon() {
  }

  public static CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder in) throws GatewayException {
    in.reference(ffu.getCaseRef());
    in.type(CaseRequest.Type.CE);
    in.category("Not applicable");
    in.estabType(ffu.getEstabType());
    in.requiredOfficer(ffu.getFieldOfficerId());
    in.coordCode(ffu.getFieldCoordinatorId());

    Contact.ContactBuilder outContact = Contact.builder();
    outContact.organisationName(ffu.getOrganisationName());
    if (cache != null) {
      List<String> fields = Lists
          .newArrayList(cache.getManagerTitle(), cache.getManagerFirstname(), cache.getManagerSurname());
      String name = fields.stream().filter(Objects::nonNull).collect(Collectors.joining(" "));
      outContact.name(name);
      if (cache.getContactPhoneNumber() != null) {
        outContact.phone(cache.getContactPhoneNumber());
      }
    }
    in.contact(outContact.build());

    Address.AddressBuilder outAddress = Address.builder();
    outAddress.lines(List.of(
        ffu.getAddressLine1(),
        Objects.toString(ffu.getAddressLine2(), ""),
        Objects.toString(ffu.getAddressLine3(), "")
    ));
    outAddress.town(ffu.getTownName());
    outAddress.postcode(ffu.getPostcode());
    in.address(outAddress.build());

    Geography.GeographyBuilder outGeography = Geography.builder();
    outGeography.oa(ffu.getOa());
    outAddress.geography(outGeography.build());

    Location.LocationBuilder outLocation = Location.builder();
    outLocation.lat(ConverterUtils.parseFloat(ffu.getLatitude()));
    outLocation._long(ConverterUtils.parseFloat(ffu.getLongitude()));
    in.location(outLocation.build());

    in.uaa(ffu.getUaa());
    in.sai(false);

    return in;
  }
}

