package com.visitly.test.unit.controller;

import com.visitly.controller.UserController;
import com.visitly.dto.*;
import com.visitly.mapper.UserMapper;
import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for the UserController class.
 *
 * Validates controller-layer logic for user registration, login, and
 * profile retrieval. Focuses on verifying response structure,
 * service interactions, and correct handling of edge cases.
 */
@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserController userController;

    private RegisterUserRequest registerRequest;
    private User user;
    private UserProfileResponse userProfileResponse;

    // Initializes common test data before each test execution.
    @BeforeEach
    void setUp() {
        registerRequest = new RegisterUserRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");

        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("password123");

        userProfileResponse = new UserProfileResponse(user.getId(), user.getUsername(), user.getEmail());
    }
    
    /**
     * Tests successful user registration through the controller.
     *
     * Verifies that the controller correctly maps requests, delegates
     * to the service layer, and returns a valid success response.
     */
    @Test
    void registerUser_Success() {
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        when(userService.registerUser(any(User.class))).thenReturn(user); // ✅ FIXED
        when(userMapper.toUserProfileResponse(user)).thenReturn(userProfileResponse);

        ResponseEntity<ApiResponse<UserProfileResponse>> response = userController.registerUser(registerRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<UserProfileResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("User registered successfully", body.getMessage());
        assertEquals(userProfileResponse, body.getData());

        verify(userMapper, times(1)).toEntity(registerRequest);
        verify(userService, times(1)).registerUser(any(User.class));
        verify(userMapper, times(1)).toUserProfileResponse(user);
    }

    /**
     * Tests registration when the email is missing.
     *
     * Expects an IllegalArgumentException and verifies that
     * the service throws the expected error message.
     */
    @Test
    void registerUser_WithNullEmail_ShouldThrowException() {
        registerRequest.setEmail(null);
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        doThrow(new IllegalArgumentException("Email and password are required."))
                .when(userService).registerUser(any(User.class));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userController.registerUser(registerRequest)
        );

        assertEquals("Email and password are required.", ex.getMessage());
        verify(userService, times(1)).registerUser(any(User.class));
    }

    /**
     * Tests registration when the provided email already exists.
     *
     * Verifies that the controller propagates the correct exception message.
     */
    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        when(userMapper.toEntity(registerRequest)).thenReturn(user);
        doThrow(new IllegalArgumentException("Email already exists."))
                .when(userService).registerUser(any(User.class));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userController.registerUser(registerRequest)
        );

        assertEquals("Email already exists.", ex.getMessage());
        verify(userService, times(1)).registerUser(any(User.class));
    }

    /**
     * Tests successful login request handling.
     *
     * Ensures that the controller returns a valid JWT token
     * and correct API response structure.
     */
    @Test
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        String mockToken = "mock.jwt.token";
        when(userService.login(any(LoginRequest.class))).thenReturn(mockToken);

        ResponseEntity<ApiResponse<TokenResponse>> response = userController.login(loginRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<TokenResponse> body = response.getBody();
        assertNotNull(body);
        assertEquals("Login successful", body.getMessage());
        assertEquals(mockToken, body.getData().getToken());

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Tests login attempt with invalid credentials.
     *
     * Expects a RuntimeException and ensures the controller
     * correctly propagates the failure message.
     */
    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("wrongpassword");

        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> userController.login(loginRequest)
        );

        assertEquals("Invalid email or password", ex.getMessage());
        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Tests successful retrieval of the currently authenticated user's profile.
     *
     * Verifies that the response includes expected user data and roles.
     */
    @Test
    void getCurrentUser_Success() {
        String email = "test@example.com";

        Role role = new Role();
        role.setRoleName("ADMIN");

        User user = new User();
        user.setId(1L);
        user.setUsername("TestUser");
        user.setEmail(email);
        user.setRoles(Set.of(role));

        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(user);

        ResponseEntity<?> response = userController.getCurrentUser(authentication);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        
        ApiResponse<?> apiResponse = (ApiResponse<?>) response.getBody();
        assertNotNull(apiResponse);
        assertEquals("User profile retrieved successfully", apiResponse.getMessage());

        Map<?, ?> data = (Map<?, ?>) apiResponse.getData();
        assertEquals("TestUser", data.get("username"));
        assertEquals(email, data.get("email"));
        assertTrue(((List<?>) data.get("roles")).contains("ADMIN"));

        verify(authentication).getName();
        verify(userService).findByEmail(email);
    }

    /**
     * Tests fetching the profile of a user that does not exist.
     *
     * Expects an IllegalArgumentException and verifies that
     * the correct message is returned.
     */
    @Test
    void getCurrentUser_UserNotFound_ShouldThrowException() {
        String email = "nonexistent@example.com";
        when(authentication.getName()).thenReturn(email);
        when(userService.findByEmail(email)).thenReturn(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userController.getCurrentUser(authentication)
        );

        assertEquals("User not found", ex.getMessage());
        verify(userService, times(1)).findByEmail(email);
    }

    /**
     * Tests profile retrieval when the authentication object is null.
     *
     * Expects a NullPointerException and ensures the service is never called.
     */
    @Test
    void getCurrentUser_WithNullAuthentication_ShouldThrowException() {
        assertThrows(NullPointerException.class, () -> userController.getCurrentUser(null));
        verify(userService, never()).findByEmail(any());
    }
}
