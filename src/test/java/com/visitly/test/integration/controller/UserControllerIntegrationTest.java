package com.visitly.test.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visitly.config.SecurityConfig;
import com.visitly.controller.UserController;
import com.visitly.dto.*;
import com.visitly.mapper.UserMapper;
import com.visitly.model.User;
import com.visitly.security.JwtUtil;
import com.visitly.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the UserController endpoints.
 *
 * Verifies the full controller layer behavior, including request mapping,
 * JSON serialization, HTTP response codes, and authentication enforcement.
 * Mocks dependencies such as UserService and JwtUtil for isolated endpoint testing.
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class) 
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserMapper userMapper;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;
    
    @MockBean
    private org.springframework.security.core.userdetails.UserDetailsService userDetailsService;

    private User testUser;
    private RegisterUserRequest registerRequest;
    private LoginRequest loginRequest;
    private UserProfileResponse profileResponse;

    // Initializes test data before each integration test run.
    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("johndoe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("password123");

        registerRequest = new RegisterUserRequest();
        registerRequest.setUsername("johndoe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        profileResponse = new UserProfileResponse(1L, "johndoe", "john@example.com");
    }

    /**
     * Tests successful registration of a new user.
     *
     * Verifies that the endpoint returns a 200 OK response with a valid JSON payload.
     */
    @Test
    void testRegisterUser_Success() throws Exception {
        when(userMapper.toEntity(any(RegisterUserRequest.class))).thenReturn(testUser);
        when(userService.registerUser(any(User.class))).thenReturn(testUser);
        when(userMapper.toUserProfileResponse(testUser)).thenReturn(profileResponse);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.data.username").value("johndoe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));

        verify(userService, times(1)).registerUser(any(User.class));
    }

    /**
     * Tests registration failure when an existing email is used.
     *
     * Ensures a 400 Bad Request response with a descriptive error message.
     */
    @Test
    void testRegisterUser_Failure_EmailExists() throws Exception {
        when(userMapper.toEntity(any(RegisterUserRequest.class))).thenReturn(testUser);
        when(userService.registerUser(any(User.class)))
                .thenThrow(new IllegalArgumentException("Email already exists"));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Email already exists"));
    }


    /**
     * Tests successful login request.
     *
     * Verifies that a JWT token is returned with a 200 OK response.
     */
    @Test
    void testLogin_Success() throws Exception {
        when(userService.login(any(LoginRequest.class))).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.data.token").value("mocked-jwt-token"));

        verify(userService, times(1)).login(any(LoginRequest.class));
    }

    /**
     * Tests login failure with invalid credentials.
     *
     * Verifies that a 5xx response is returned and includes an appropriate error message.
     */
    @Test
    void testLogin_Failure_InvalidCredentials() throws Exception {
        when(userService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("Invalid email or password"));

        mockMvc.perform(post("/api/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is5xxServerError())
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }


    /**
     * Tests successful retrieval of current user profile when authenticated.
     *
     * Ensures that valid user data is returned in the JSON response.
     */
    @Test
    @WithMockUser(username = "john@example.com", roles = {"USER"})
    void testGetCurrentUser_Success() throws Exception {
        when(userService.findByEmail("john@example.com")).thenReturn(testUser);
        when(userMapper.toUserProfileResponse(testUser)).thenReturn(profileResponse);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("User profile retrieved successfully"))
                .andExpect(jsonPath("$.data.username").value("johndoe"))
                .andExpect(jsonPath("$.data.email").value("john@example.com"));

        verify(userService, times(1)).findByEmail("john@example.com");
    }

    /**
     * Tests retrieval of current user profile when the user does not exist.
     *
     * Expects a 400 Bad Request with an appropriate "User not found" message.
     */
    @Test
    @WithMockUser(username = "ghost@example.com", roles = {"USER"})
    void testGetCurrentUser_NotFound() throws Exception {
        when(userService.findByEmail("ghost@example.com")).thenReturn(null);

        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    /**
     * Tests unauthorized access to the /me endpoint.
     *
     * Ensures that unauthenticated requests are blocked with a 403 Forbidden status.
     */
    @Test
    void testGetCurrentUser_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/users/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
}
