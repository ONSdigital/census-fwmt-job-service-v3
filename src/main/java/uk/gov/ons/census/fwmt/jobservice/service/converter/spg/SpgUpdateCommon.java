package uk.gov.ons.census.fwmt.jobservice.service.converter.spg;

import com.google.common.collect.Lists;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;
import uk.gov.ons.census.fwmt.jobservice.service.converter.ConverterUtils;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class SpgUpdateCommon {

  private SpgUpdateCommon() {
  }

  public static CaseRequest.CaseRequestBuilder convert(FieldworkFollowup ffu, GatewayCache cache,
      CaseRequest.CaseRequestBuilder builder) throws GatewayException {
    builder.reference(ffu.getCaseRef());
    builder.type(CaseRequest.Type.CE);
    builder.category("Not applicable");
    builder.estabType(ffu.getEstabType());
    builder.requiredOfficer(ffu.getFieldOfficerId());
    builder.coordCode(ffu.getFieldCoordinatorId());

    Contact.ContactBuilder outContact = Contact.builder();
    outContact.organisationName(ffu.getOrganisationName());
    builder.contact(outContact.build());

    Address.AddressBuilder outAddress = Address.builder();
    outAddress.lines(List.of(
        ffu.getAddressLine1(),
        Objects.toString(ffu.getAddressLine2(), ""),
        Objects.toString(ffu.getAddressLine3(), "")
    ));
    outAddress.town(ffu.getTownName());
    outAddress.postcode(ffu.getPostcode());
    builder.address(outAddress.build());

    Geography.GeographyBuilder outGeography = Geography.builder();
    outGeography.oa(ffu.getOa());
    outAddress.geography(outGeography.build());

    Location.LocationBuilder outLocation = Location.builder();
    outLocation.lat(ConverterUtils.parseFloat(ffu.getLatitude()));
    outLocation._long(ConverterUtils.parseFloat(ffu.getLongitude()));
    builder.location(outLocation.build());

    builder.uaa(ffu.getUaa());
    builder.sai(false);

    return builder;
  }
}

