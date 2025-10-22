package com.visitly.service.kafka;

import com.visitly.events.UserEvent;
import com.visitly.service.AuditService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * Kafka consumer service responsible for processing user-related events.
 *
 * Listens to registration and login event topics, logs received messages,
 * and records corresponding audit entries for system traceability.
 * 
 */
@Service
public class KafkaConsumerService {

    private static final Logger logger = LogManager.getLogger(KafkaConsumerService.class);
    
    @Autowired
    private AuditService auditService;

    /**
     * Consumes user registration events from the Kafka topic.
     *
     * Logs the event details and records the outcome (success or failure)
     * in the audit log for tracking user registration activity.
     *
     * @param event The user registration event message received from Kafka
     */
    @KafkaListener(topics = "user-registration-events", groupId = "visitly-consumer-group")
    public void consumeRegistrationEvent(UserEvent event) {
        logger.info("[CONSUMER] Received registration event for user: {}", event.getEmail());
        logger.debug("Event type: {}, Timestamp: {}", event.getEventType(), event.getTimestamp());
        
        String message = event.getStatus().equalsIgnoreCase("Failure")
                ? "User registration failed: " + event.getMessage()
                : "New user registered successfully.";
        
        auditService.recordAction("USER_REGISTRATION", event.getStatus(), event.getEmail(), message);
    }

    /**
     * Consumes user login events from the Kafka topic.
     *
     * Logs the event details and records the outcome (success or failure)
     * in the audit log for tracking authentication activity.
     *
     * @param event The user login event message received from Kafka
     */
    @KafkaListener(topics = "user-login-events", groupId = "visitly-consumer-group")
    public void consumeLoginEvent(UserEvent event) {
        logger.info("[CONSUMER] Received login event for user: {}", event.getEmail());
        logger.debug("Event type: {}, Timestamp: {}", event.getEventType(), event.getTimestamp());
        
        String message = event.getStatus().equalsIgnoreCase("Failure")
                ? "User login failed: " + event.getMessage()
                : "User logged in successfully.";
        
        auditService.recordAction("USER_LOGIN", event.getStatus(), event.getEmail(), message);
    }
}
