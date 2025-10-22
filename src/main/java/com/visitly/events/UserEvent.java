package com.visitly.events;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a user-related system event, such as registration or login.
 *
 * Used for publishing event data to Kafka topics and tracking event status
 * across the application.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserEvent {
	
	// Type of the event (eg. USER_REGISTERED, USER_LOGIN)
    private String eventType;
    
    // Status of event (SUCCESS or FAILURE)
    private String status;
    
    // Email address of user associated with this event
    private String email;
    
    // Timestamp of event recording
    private LocalDateTime timestamp;
    
    // Descriptive message included in event report
    private String message;
}
