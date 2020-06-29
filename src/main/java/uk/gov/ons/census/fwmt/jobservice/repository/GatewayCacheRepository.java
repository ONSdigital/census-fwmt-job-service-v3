package uk.gov.ons.census.fwmt.jobservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

import java.util.List;

@Repository
public interface GatewayCacheRepository extends JpaRepository<GatewayCache, Long> {
  GatewayCache findByCaseId(String caseId);

  boolean existsByEstabUprn(String uprn);

  boolean existsByEstabUprnAndType(String estabUprn, int type);

  @Query("SELECT estab.caseId FROM GatewayCache estab WHERE estab.estabUprn = :estabUprn")
  String findByEstabUprn(@Param("estabUprn") String estabUprn);

  @NonNull
  List<GatewayCache> findAll();

}
