package com.velmart.service;

import com.velmart.model.AuditLog;
import com.velmart.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {

    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void logAction(String actorId, String actorEmail, String role, String action, String targetType, String targetId, String details, String ipAddress) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setAuditId(UUID.randomUUID().toString());
            auditLog.setActorId(actorId != null ? actorId : "SYSTEM");
            auditLog.setActorEmail(actorEmail != null ? actorEmail : "system@velmart.com");
            auditLog.setRole(role != null ? role : "SYSTEM");
            auditLog.setAction(action);
            auditLog.setTargetType(targetType);
            auditLog.setTargetId(targetId);
            auditLog.setDetails(details);
            auditLog.setIpAddress(ipAddress != null ? ipAddress : "127.0.0.1");
            auditLog.setTimestamp(Instant.now().toString());

            auditLogRepository.save(auditLog);
            log.info("[AUDIT] Actor: {}, Action: {}, Target: {}:{}", actorEmail, action, targetType, targetId);
        } catch (Exception e) {
            log.error("Failed to persist audit log: {}", e.getMessage());
        }
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }

    public List<AuditLog> getLogsByActor(String actorId) {
        return auditLogRepository.findByActorId(actorId);
    }
}
