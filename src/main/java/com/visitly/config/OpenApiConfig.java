package com.visitly.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for OpenAPI (Swagger) documentation.
 * 
 * Provides metadata for the Visitly REST API, including title, description,
 * version, contact information, and license details.
 * 
 */

@Configuration
public class OpenApiConfig {

	/**
     * Builds and configures the OpenAPI documentation details for the application.
     *
     * @return an OpenAPI instance containing API metadata and contact information
     */
    @Bean
    public OpenAPI visitlyOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("User Service API Documentation")
                        .description("REST API documentation for the User, Role, and Admin modules.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Visitly Dev Team")
                                .email("support@visitly.com")
                                .url("https://visitly.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://springdoc.org")));
    }
}
