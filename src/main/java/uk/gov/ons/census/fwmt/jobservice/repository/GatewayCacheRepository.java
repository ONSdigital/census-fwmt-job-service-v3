package uk.gov.ons.census.fwmt.jobservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;

@Repository
public interface GatewayCacheRepository extends JpaRepository<GatewayCache, Long> {
  @NonNull
  GatewayCache findByCaseId(String caseId);

  @NonNull
  List<GatewayCache> findAll();
}
