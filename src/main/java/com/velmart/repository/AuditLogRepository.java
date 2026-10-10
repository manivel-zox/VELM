package com.velmart.repository;

import com.velmart.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    List<AuditLog> findByActorIdOrderByTimestampDesc(String actorId);

    default List<AuditLog> findByActorId(String actorId) {
        return findByActorIdOrderByTimestampDesc(actorId);
    }

    List<AuditLog> findAllByOrderByTimestampDesc();

    default void delete(String auditId) {
        deleteById(auditId);
    }
}
