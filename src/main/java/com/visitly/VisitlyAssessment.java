package com.visitly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.EnableKafka;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@SpringBootApplication
@EnableCaching
@EnableKafka
public class VisitlyAssessment {

	public static void main(String[] args) {	    
		SpringApplication.run(VisitlyAssessment.class, args);
	}
	
	
	/**
     * Registers a custom Jackson module to properly handle Java Time API
     * during JSON serialization and deserialization.
     */
	@Bean
    public Jackson2ObjectMapperBuilderCustomizer jsonCustomizer() {
        return builder -> builder.modulesToInstall(new JavaTimeModule());
    }
}
