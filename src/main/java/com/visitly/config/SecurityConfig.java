package com.visitly.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.visitly.security.JwtAuthFilter;


/**
 * Configures application-wide Spring Security settings.
 * 
 * Enables stateless JWT-based authentication, method-level security
 * and exposes public endpoints for user registration, login, and API documentation.
 */

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

	@Autowired
	private JwtAuthFilter jwtAuthFilter;

	/**
     * Defines the security filter chain for HTTP requests.
     * 
     * - Disables CSRF protection (since JWT is stateless).  
     * - Allows unauthenticated access to login, registration, and Swagger endpoints.  
     * - Requires authentication for all other requests.  
     * - Adds a custom JWT authentication filter before the username/password filter.  
     *
     *
     * @param http the HttpSecurity builder
     * @return a configured SecurityFilterChain
     * @throws Exception if the security configuration fails
     */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
	    http
	        .csrf(csrf -> csrf.disable())
	        .authorizeHttpRequests(auth -> auth
	        	.requestMatchers(
                            "/v3/api-docs/**",
                            "/swagger-ui/**",
                            "/swagger-ui.html")
	        	.permitAll()
	            .requestMatchers("/api/users/register", "/api/users/login").permitAll()
	            .anyRequest().authenticated()
	        )
	        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
	        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

	    return http.build();
	}
	
	/**
     * Provides a BCrypt-based password encoder for securing user credentials.
     *
     * @return a {@link PasswordEncoder} instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes the {@link AuthenticationManager} bean for authentication processes.
     *
     * @param configuration the Spring authentication configuration
     * @return an {@link AuthenticationManager} instance
     * @throws Exception if retrieval fails
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
