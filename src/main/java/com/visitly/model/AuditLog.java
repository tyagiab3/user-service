package com.visitly.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing an audit record in the system.
 *
 * Stores details about significant user or system actions,
 * including the action type, status, performer, and timestamp.
 * 
 */
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

	// Unique identifier for the audit record
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Type of action performed (eg., USER_CREATED, ROLE_ASSIGNED)
    private String actionType;
    
    // Status of the action (eg., SUCCESS, FAILURE)
    private String status;
    
    // Identifier of the user or process that performed the action
    private String performedBy;
    
    // Additional descriptive details about the action
    private String details;
    
    // Timestamp recording when the action occurred
    private LocalDateTime timestamp;
    
    /**
     * Convenience constructor for creating a new audit log entry.
     * Usage abstains from entering id as it is auto-generated.
     * Automatically sets the timestamp to the current time.
     *
     * @param actionType The type of action performed
     * @param status The outcome of the action
     * @param performedBy The user or process that performed the action
     * @param details Additional information about the action
     */
    public AuditLog(String actionType, String status, String performedBy, String details) {
        this.actionType = actionType;
        this.status = status;
        this.performedBy = performedBy;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

}
