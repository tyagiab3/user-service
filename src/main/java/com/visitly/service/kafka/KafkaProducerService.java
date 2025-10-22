package com.visitly.service.kafka;

import com.visitly.events.UserEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Kafka producer service responsible for publishing user-related events.
 *
 * Sends user registration and login events to dedicated Kafka topics
 * to support asynchronous auditing and monitoring of user activity.
 * 
 */
@Service
public class KafkaProducerService {
	
	private static final Logger logger = LogManager.getLogger(KafkaProducerService.class);

    private static final String REGISTRATION_TOPIC = "user-registration-events";
    private static final String LOGIN_TOPIC = "user-login-events";

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Publishes a user registration event to the Kafka topic.
     *
     * Sends the event synchronously and logs both success and failure cases.
     *
     * @param event The user registration event to publish
     */
    public void sendRegistrationEvent(UserEvent event) {
    	try {
            kafkaTemplate.send(REGISTRATION_TOPIC, event).get();
            logger.info("[PRODUCER] Event sent synchronously: {} (status={})", event.getEmail(), event.getStatus());
        } catch (Exception e) {
            logger.error("[PRODUCER] Failed to send event for {} - {}", event.getEmail(), e.getMessage());
        }
    }

    /**
     * Publishes a user login event to the Kafka topic.
     *
     * Sends the event synchronously and logs both success and failure cases.
     *
     * @param event The user login event to publish
     */
    public void sendLoginEvent(UserEvent event) {
    	try {
            kafkaTemplate.send(LOGIN_TOPIC, event).get();
            logger.info("[PRODUCER] Event sent synchronously: {} (status={})", event.getEmail(), event.getStatus());
        } catch (Exception e) {
            logger.error("[PRODUCER] Failed to send event for {} - {}", event.getEmail(), e.getMessage());
        }
    }
}
