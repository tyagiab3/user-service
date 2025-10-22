package com.visitly.test.unit.service;

import com.visitly.dto.LoginRequest;
import com.visitly.events.UserEvent;
import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.repository.UserRepository;
import com.visitly.security.JwtUtil;
import com.visitly.service.AuditService;
import com.visitly.service.UserService;
import com.visitly.service.kafka.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the UserService class.
 *
 * Validates user registration, login, and retrieval logic, ensuring
 * correct interactions with dependent services such as Kafka producers,
 * JWT utilities, and repositories.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private AuditService auditService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder;

    // Initializes the password encoder before each test execution.
    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Tests successful user registration.
     *
     * Verifies that the user is saved, the password is encrypted,
     * and a Kafka registration event is sent.
     */
    @Test
    void registerUser_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setUsername("testuser");
        user.setPassword("password123");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(user);

        userService.registerUser(user);

        verify(userRepository, times(1)).findByEmail(user.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(kafkaProducerService, times(1))
                .sendRegistrationEvent(any(UserEvent.class));

        assertNotEquals("password123", user.getPassword());
    }

    /**
     * Tests user registration with a missing email field.
     *
     * Expects an IllegalArgumentException and verifies no database interaction occurs.
     */
    @Test
    void registerUser_WithNullEmail_ShouldThrowException() {
        User user = new User();
        user.setPassword("password123");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(user)
        );

        assertEquals("Email and password are required.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    /**
     * Tests user registration when an existing email already exists in the database.
     *
     * Ensures an exception is thrown and no duplicate user is persisted.
     */
    @Test
    void registerUser_WithExistingEmail_ShouldThrowException() {
        User existingUser = new User();
        existingUser.setEmail("existing@example.com");

        User newUser = new User();
        newUser.setEmail("existing@example.com");
        newUser.setPassword("password123");

        when(userRepository.findByEmail(newUser.getEmail())).thenReturn(Optional.of(existingUser));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> userService.registerUser(newUser)
        );

        assertEquals("Email already exists.", exception.getMessage());

    }

    /**
     * Tests successful login with valid credentials.
     *
     * Verifies JWT token generation, Kafka event publishing,
     * and user repository updates for last login time.
     */
    @Test
    void login_Success() {
        String email = "test@example.com";
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);

        Role role1 = new Role();
        role1.setRoleName("USER");

        Role role2 = new Role();
        role2.setRoleName("ADMIN");

        Set<Role> roles = new HashSet<>(Arrays.asList(role1, role2));
        user.setRoles(roles);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        String mockToken = "mock.jwt.token";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn(mockToken);
        when(userRepository.save(any(User.class))).thenReturn(user);

        String token = userService.login(loginRequest);

        assertNotNull(token);
        assertEquals(mockToken, token);
        verify(userRepository, times(1)).findByEmail(email);
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtUtil, times(1)).generateToken(eq(email), anyList());
        verify(kafkaProducerService, times(1))
                .sendLoginEvent(any(UserEvent.class));
        assertNotNull(user.getLastLogin());
    }

    /**
     * Tests login with a non-existent email address.
     *
     * Expects a RuntimeException and verifies a Kafka failure event is published.
     */
    @Test
    void login_WithInvalidEmail_ShouldThrowException() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail("nonexistent@example.com");
        loginRequest.setPassword("password123");

        when(userRepository.findByEmail(loginRequest.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginRequest)
        );

        assertEquals("Invalid email or password.", exception.getMessage());

        verify(kafkaProducerService, times(1))
                .sendLoginEvent(any(UserEvent.class));
    }

    /**
     * Tests login with an incorrect password.
     *
     * Ensures authentication fails and a Kafka event is sent for the failure.
     */
    @Test
    void login_WithInvalidPassword_ShouldThrowException() {
        String email = "test@example.com";
        String correctPassword = "password123";
        String wrongPassword = "wrongpassword";
        String hashedPassword = passwordEncoder.encode(correctPassword);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(wrongPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.login(loginRequest)
        );

        assertEquals("Invalid email or password.", exception.getMessage());

        verify(kafkaProducerService, times(1))
                .sendLoginEvent(any(UserEvent.class));
    }

    // Tests that successful login updates the user's last login timestamp.
    @Test
    void login_UpdatesLastLoginTime() {
        String email = "test@example.com";
        String rawPassword = "password123";
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User();
        user.setEmail(email);
        user.setPassword(hashedPassword);
        user.setRoles(new HashSet<>());

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setEmail(email);
        loginRequest.setPassword(rawPassword);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(anyString(), anyList())).thenReturn("token");
        when(userRepository.save(any(User.class))).thenReturn(user);

        LocalDateTime beforeLogin = LocalDateTime.now();

        userService.login(loginRequest);

        assertNotNull(user.getLastLogin());
        assertTrue(user.getLastLogin().isAfter(beforeLogin.minusSeconds(1)));
        verify(userRepository, times(1)).save(user);
        verify(kafkaProducerService, times(1))
                .sendLoginEvent(any(UserEvent.class));
    }

    /**
     * Tests fetching a user by email when the user exists.
     *
     * Verifies that the returned user matches the expected data.
     */
    @Test
    void findByEmail_UserExists_ReturnsUser() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);
        user.setUsername("testuser");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        User foundUser = userService.findByEmail(email);

        assertNotNull(foundUser);
        assertEquals(email, foundUser.getEmail());
        assertEquals("testuser", foundUser.getUsername());
        verify(userRepository, times(1)).findByEmail(email);
    }

    /**
     * Tests fetching a user by email when the user does not exist.
     *
     * Expects a null result and verifies the repository is called exactly once.
     */
    @Test
    void findByEmail_UserDoesNotExist_ReturnsNull() {
        String email = "nonexistent@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        User foundUser = userService.findByEmail(email);

        assertNull(foundUser);
        verify(userRepository, times(1)).findByEmail(email);
    }
}

