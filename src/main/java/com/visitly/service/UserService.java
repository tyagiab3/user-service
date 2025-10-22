package com.visitly.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.visitly.dto.LoginRequest;
import com.visitly.events.UserEvent;
import com.visitly.model.User;
import com.visitly.repository.UserRepository;
import com.visitly.security.JwtUtil;
import com.visitly.service.kafka.KafkaProducerService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service layer responsible for user management and authentication operations.
 *
 * Handles user registration, login, caching, and integrates with Kafka for
 * event publishing and the audit logging service for data tracking.
 * 
 */
@Service
public class UserService {

	private static final Logger logger = LogManager.getLogger(UserService.class);
	
	@Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    @Autowired
    private KafkaProducerService kafkaProducerService;
    
    @Autowired
    private AuditService auditService;
    
    /**
     * Registers a new user in the system.
     *
     * Validates required fields, hashes the password, and persists the user.
     * Produces Kafka events for both success and failure scenarios.
     *
     * @param user The user entity containing registration data
     * @return The newly registered User entity
     * @throws IllegalArgumentException If the email or password is missing or already exists
     */
    @CacheEvict(value = "users", key = "#user.email", condition = "#user.email != null")
    public User registerUser(User user) {

        logger.info("Registering new user with email: {}", user.getEmail());

        if (user.getEmail() == null || user.getPassword() == null) {
            String message = "Email and password are required.";
            logger.error(message);
            kafkaProducerService.sendRegistrationEvent(
                    new UserEvent("REGISTRATION_FAILURE", "Failure", user.getEmail(), LocalDateTime.now(), message)
            );
            throw new IllegalArgumentException(message);
        }

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            String message = "Email already exists.";
            logger.warn(message);
            kafkaProducerService.sendRegistrationEvent(
                    new UserEvent("REGISTRATION_FAILURE", "Failure", user.getEmail(), LocalDateTime.now(), message)
            );
            throw new IllegalArgumentException(message);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User savedUser = userRepository.save(user);

        logger.info("User registered successfully: {}", savedUser.getEmail());
        kafkaProducerService.sendRegistrationEvent(
                new UserEvent("USER_REGISTERED", "Success", savedUser.getEmail(), LocalDateTime.now(), null)
        );
        logger.info("[KAFKA] Registration event message produced successfully.");
        return savedUser;
    }

    /**
     * Authenticates a user and generates a JWT token upon successful login.
     *
     * Validates credentials, updates the user's last login timestamp,
     * publishes Kafka login events, and refreshes the user cache.
     *
     * @param request The login credentials containing email and password
     * @return A signed JWT token for the authenticated user
     * @throws RuntimeException If authentication fails due to invalid credentials
     */
    public String login(LoginRequest request) {
    	logger.info("Login request for email: {}", request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);
        
        if (user == null) {
            String message = "Invalid email or password.";
            logger.warn("Login failed — user not found: {}", request.getEmail());
            kafkaProducerService.sendLoginEvent(
                    new UserEvent("LOGIN_FAILURE", "Failure", request.getEmail(), LocalDateTime.now(), message)
            );
            throw new RuntimeException(message);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        	String message = "Invalid email or password.";
        	logger.warn("Invalid password for user: {}", request.getEmail());
        	kafkaProducerService.sendLoginEvent(
                    new UserEvent("LOGIN_FAILURE", "Failure", user.getEmail(), LocalDateTime.now(), message)
            );
            throw new RuntimeException(message);
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);
        updateCache(user);
        
        List<String> roles = user.getRoles()
                .stream()
                .map(role -> role.getRoleName())
                .collect(Collectors.toList());

        String token = jwtUtil.generateToken(user.getEmail(), roles);
        logger.info("JWT token generated for user: {}", user.getEmail());
        kafkaProducerService.sendLoginEvent(
                new UserEvent("USER_LOGGED_IN", "Success", user.getEmail(), LocalDateTime.now(), null)
        );
        logger.info("[KAFKA] Login event message produced successfully.");
        return token;
    }
    
    /**
     * Retrieves a user by email address.
     *
     * Uses caching to improve lookup performance and logs
     * the result to the audit service.
     *
     * @param email The email address to search for
     * @return The User entity if found, or null if not found
     */
    @Cacheable(value = "users", key = "#email")
    public User findByEmail(String email) {
    	logger.debug("Fetching user from database: {}", email);
    	
    	User user = userRepository.findByEmail(email).orElse(null);
        

        if (user == null) {
            logger.warn("User not found for email: {}", email);
            auditService.recordAction("USER_DATA_FETCH", "Failure", email, "User data not found for " + email);
        } else {
            logger.info("User data fetched from database for {}", email);
            auditService.recordAction("USER_DATA_FETCH", "Success", user.getEmail(), "Fetched user data for " + email);
        }

        return user;
    }
    
    /**
     * Updates the cached user data after login or modification.
     *
     * @param user The user entity to refresh in the cache
     * @return The same user entity after cache update
     */
    @CachePut(value = "users", key = "#user.email")
    public User updateCache(User user) {
    	logger.debug("Updating cache for user: {}", user.getEmail());
        return user;
    }
    
}
