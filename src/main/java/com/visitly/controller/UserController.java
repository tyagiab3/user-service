package com.visitly.controller;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.visitly.dto.ApiResponse;
import com.visitly.dto.LoginRequest;
import com.visitly.dto.RegisterUserRequest;
import com.visitly.dto.TokenResponse;
import com.visitly.dto.UserProfileResponse;
import com.visitly.mapper.UserMapper;
import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * REST controller for user registration, authentication, and profile management.
 * 
 * Provides endpoints for creating new user accounts, logging in to retrieve
 * JWT tokens, and fetching authenticated user details.
 * 
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user registration, login, and profile retrieval")
public class UserController {

	private static final Logger logger = LogManager.getLogger(UserController.class);
	
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserMapper userMapper;

    /**
     * Registers a new user in the system.
     * 
     * Validates incoming user data, hashes the password, and persists the new user.
     * Returns a response containing the newly created user’s profile details.
     * 
     *
     * @param request the registration payload containing username, email, and password
     * @return an ApiResponse containing the registered user’s profile
     */
    @Operation(summary = "Register a new user", description = "Creates a new user account in the system.")
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserProfileResponse>> registerUser(@Valid @RequestBody RegisterUserRequest request) {
    	
    	logger.info("Received registration request for email: {}", request.getEmail());
    	User userEntity = userMapper.toEntity(request);
    	
    	User savedUser = userService.registerUser(userEntity);
    	
    	UserProfileResponse response = userMapper.toUserProfileResponse(savedUser);
        logger.info("User registered successfully: {}", savedUser.getEmail());
        
    	return ResponseEntity.ok(new ApiResponse<>(true, "User registered successfully", response));

    }
    
    /**
     * Authenticates a user and generates a JWT token upon successful login.
     * 
     * The token is returned in the response and can be used for subsequent
     * authenticated requests.
     * 
     *
     * @param request the login credentials (email and password)
     * @return an ApiResponse containing a valid JWT token
     */
    @Operation(summary = "Login user", description = "Authenticates the user and returns a JWT token.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
    	
    	logger.info("Login attempt for email: {}", request.getEmail());
    	TokenResponse tokenResponse = new TokenResponse(userService.login(request));
    	logger.info("Login successful for email: {}", request.getEmail());
        
    	return ResponseEntity.ok(new ApiResponse<>(true, "Login successful", tokenResponse));
    }
    
    /**
     * Retrieves profile information for the currently authenticated user.
     * 
     * Extracts the user’s email from the security context, fetches user details,
     * and returns basic profile data including roles.
     * 
     *
     * @param authentication the current authentication context
     * @return an ApiResponse containing user profile details
     */
    @Operation(summary = "Get current user details", description = "Returns profile information of the currently logged-in user.")
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(org.springframework.security.core.Authentication authentication) {
        
    	String email = authentication.getName();
    	
    	logger.debug("Fetching profile for authenticated user: {}", email);
    	
    	User foundUser = userService.findByEmail(email);

        if (foundUser == null) {
            logger.warn("User not found for email: {}", email);
            throw new IllegalArgumentException("User not found");
        }

        List<String> roles = foundUser.getRoles().stream()
                .map(Role::getRoleName)
                .toList();

        Map<String, Object> responseData = Map.of(
                "id", foundUser.getId(),
                "username", foundUser.getUsername(),
                "email", foundUser.getEmail(),
                "roles", roles
        );


        logger.info("User profile retrieved for email: {}", email);

        return ResponseEntity.ok(
                new ApiResponse<>(true, "User profile retrieved successfully", responseData)
        );
    }
}