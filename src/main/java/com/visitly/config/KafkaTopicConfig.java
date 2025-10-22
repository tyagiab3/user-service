package com.visitly.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


/**
 * Kafka topic configuration class.
 * 
 * Defines and registers Kafka topics used for 
 * publishing user registration and login events.
 * 
 */

@Configuration
public class KafkaTopicConfig {

	/**
     * Defines a Kafka topic for user registration events.
     *
     * @return a NewTopic instance for the "user-registration-events" topic
     */
    @Bean
    public NewTopic registrationTopic() {
        return new NewTopic("user-registration-events", 1, (short) 1);
    }
    
    
    /**
     * Defines a Kafka topic for user login events.
     *
     * @return a NewTopic instance for the "user-login-events" topic
     */
    @Bean
    public NewTopic loginTopic() {
        return new NewTopic("user-login-events", 1, (short) 1);
    }
}
