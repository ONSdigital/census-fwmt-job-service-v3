package uk.gov.ons.census.fwmt.jobservice.repository;

import javax.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import uk.gov.ons.census.fwmt.jobservice.data.GatewayCache;

@Repository
public interface GatewayCacheRepository extends JpaRepository<GatewayCache, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  GatewayCache findByCaseId(String caseId);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  boolean existsByEstabUprn(String uprn);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  boolean existsByEstabUprnAndType(String estabUprn, int type);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT estab.caseId FROM GatewayCache estab WHERE estab.estabUprn = :estabUprn")
  String findByEstabUprn(@Param("estabUprn") String estabUprn);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT estab.caseId FROM GatewayCache estab WHERE estab.uprn = :uprn")
  String findByUprn(@Param("uprn") String uprn);

//  @Lock(LockModeType.PESSIMISTIC_WRITE)
//  @NonNull
//  List<GatewayCache> findAll();

}
