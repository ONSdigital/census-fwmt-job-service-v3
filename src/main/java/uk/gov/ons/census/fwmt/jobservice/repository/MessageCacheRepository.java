package uk.gov.ons.census.fwmt.jobservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.ons.census.fwmt.jobservice.data.MessageCache;

@Repository
public interface MessageCacheRepository extends JpaRepository<MessageCache, Long> {
  MessageCache findByCaseIdAndAndMessageType(String caseId, String messageType);

  boolean existsByCaseId(String caseId);

  boolean existsByCaseIdAndMessageType(String caseId, String messageType);

  MessageCache deleteByCaseId(String caseId);

  MessageCache findByCaseId(String caseId);

}
