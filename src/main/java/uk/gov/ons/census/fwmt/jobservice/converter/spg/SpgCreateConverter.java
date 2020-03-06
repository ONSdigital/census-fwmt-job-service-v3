package uk.gov.ons.census.fwmt.jobservice.converter.spg;

import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.common.data.modelcase.Address;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest.Type;
import uk.gov.ons.census.fwmt.common.data.modelcase.Contact;
import uk.gov.ons.census.fwmt.common.data.modelcase.Geography;
import uk.gov.ons.census.fwmt.common.data.modelcase.Location;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.converter.ConverterUtils;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
@Qualifier("Toplevel")
public final class SpgCreateConverter implements CometConverter {

  private final List<CometConverter> selectors;

  public SpgCreateConverter(@Qualifier("SPG") List<CometConverter> selectors) {
    this.selectors = selectors;
  }

  @Override
  public CaseRequest.CaseRequestBuilder convert(
      FieldworkFollowup ffu, GatewayCache cache, CaseRequest.CaseRequestBuilder out) throws GatewayException {
    out.reference(ffu.getCaseRef());
    out.type(Type.CE);
    out.category("Not applicable");
    out.estabType(ffu.getEstabType());
    out.requiredOfficer(ffu.getFieldOfficerId());
    out.coordCode(ffu.getFieldCoordinatorId());

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
    out.contact(outContact.build());

    Address.AddressBuilder outAddress = Address.builder();
    outAddress.lines(List.of(
        ffu.getAddressLine1(),
        Objects.toString(ffu.getAddressLine2(), ""),
        Objects.toString(ffu.getAddressLine3(), "")
    ));
    outAddress.town(ffu.getTownName());
    outAddress.postcode(ffu.getPostcode());
    out.address(outAddress.build());

    Geography.GeographyBuilder outGeography = Geography.builder();
    outGeography.oa(ffu.getOa());
    outAddress.geography(outGeography.build());

    Location.LocationBuilder outLocation = Location.builder();
    outLocation.lat(ConverterUtils.parseFloat(ffu.getLatitude()));
    outLocation._long(ConverterUtils.parseFloat(ffu.getLongitude()));
    out.location(outLocation.build());

    if (cache != null) {
      out.description(cache.getCareCodes());
      out.specialInstructions(cache.getAccessInfo());
    }

    out.uaa(ffu.getUaa());
    out.sai(false);

    out = ConverterUtils.getConverter(ffu, cache, selectors).convert(ffu, cache, out);

    return out;
  }

  @Override
  public Boolean isValid(FieldworkFollowup ffu, GatewayCache cache) {
    return ffu != null
        && ffu.getActionInstruction() != null && ffu.getActionInstruction().equals("CREATE")
        && ffu.getActionInstruction() != null && ffu.getSurveyName().equals("Census")
        && ffu.getAddressType() != null && ffu.getAddressType().equals("SPG")
        && ConverterUtils.maybeGetConverter(ffu, cache, selectors).isPresent();
  }

  @Bean
  public List<CometConverter> converters() {
    return List.of(new SpgCreateUnitFollowupConverter(null));
  }
}

