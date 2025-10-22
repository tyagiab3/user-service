package com.visitly.repository;

import com.visitly.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for managing audit log records.
 *
 * Provides CRUD operations for the AuditLog entity using Spring Data JPA.
 * 
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
