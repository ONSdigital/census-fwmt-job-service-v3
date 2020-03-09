package uk.gov.ons.census.fwmt.jobservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.ons.census.fwmt.common.data.modelcase.CaseRequest;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.common.rm.dto.FieldworkFollowup;
import uk.gov.ons.census.fwmt.jobservice.converter.CometConverter;
import uk.gov.ons.census.fwmt.jobservice.converter.ConverterUtils;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;

@Service
@Slf4j
public class ConverterService {
  private final List<CometConverter> selectors;
  private final GatewayCacheService cacheService;

  public ConverterService(List<CometConverter> selectors, GatewayCacheService cacheService) {
    this.selectors = selectors;
    this.cacheService = cacheService;
  }

  public CaseRequest buildPutCaseRequest(FieldworkFollowup ffu) throws GatewayException {
    GatewayCache cache = cacheService.getById(ffu.getCaseId());
    return ConverterUtils.getConverter(ffu, cache, selectors).convert(ffu, cache, CaseRequest.builder()).build();
  }
}
