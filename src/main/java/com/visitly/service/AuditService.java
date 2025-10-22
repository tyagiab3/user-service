package com.visitly.service;

import com.visitly.model.AuditLog;
import com.visitly.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Service layer responsible for recording system audit logs.
 *
 * Captures important actions performed within the application,
 * including user activity and administrative operations, and
 * persists them to the audit log repository.
 * 
 */
@Service
public class AuditService {

    private static final Logger logger = LogManager.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Records an audit entry with the specified action details.
     *
     * @param actionType The type of action performed (e.g., USER_LOGIN, ROLE_ASSIGNMENT)
     * @param status The outcome of the action (e.g., SUCCESS, FAILURE)
     * @param performedBy The user or process responsible for the action
     * @param details Additional descriptive details about the action
     */
    public void recordAction(String actionType, String status, String performedBy, String details) {
        AuditLog log = new AuditLog(actionType, status, performedBy, details);
        auditLogRepository.save(log);
        logger.info("[AUDIT] {} by {} - {}", actionType, performedBy, details);
    }
}
