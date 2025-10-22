package com.visitly.test.unit.service;

import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.repository.RoleRepository;
import com.visitly.repository.UserRepository;
import com.visitly.service.AuditService;
import com.visitly.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the RoleService class.
 *
 * Verifies role creation, role assignment to users, and the handling
 * of duplicate or invalid data. Ensures correct audit logging and repository
 * interactions across both success and failure scenarios.
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private RoleService roleService;

    private Role testRole;
    private User testUser;

    // Initializes common mock data for each test run.	
    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("USER");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setRoles(new HashSet<>());
    }

    /**
     * Tests successful creation of a new role.
     *
     * Verifies that a role is persisted, audit logging is triggered,
     * and duplicate detection does not interfere.
     */
    @Test
    void createRole_Success() {
        String roleName = "ADMIN";
        Role newRole = new Role();
        newRole.setId(2L);
        newRole.setRoleName(roleName);

        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.empty());
        when(roleRepository.save(any(Role.class))).thenReturn(newRole);

        Role createdRole = roleService.createRole(roleName);

        assertNotNull(createdRole);
        assertEquals(roleName, createdRole.getRoleName());
        assertEquals(2L, createdRole.getId());

        verify(roleRepository, times(1)).findByRoleName(roleName);
        verify(roleRepository, times(1)).save(any(Role.class));
        verify(auditService, times(1))
                .recordAction(eq("ROLE_CREATION"), eq("Success"), anyString(), contains("created successfully"));
    }

    /**
     * Tests attempting to create a role that already exists.
     *
     * Expects a RuntimeException and verifies that no new role is saved.
     */
    @Test
    void createRole_WithExistingRole_ShouldThrowException() {

        String roleName = "USER";
        when(roleRepository.findByRoleName(roleName)).thenReturn(Optional.of(testRole));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleService.createRole(roleName)
        );

        assertEquals("Role already exists", exception.getMessage());
        verify(auditService, times(1))
                .recordAction(eq("ROLE_CREATION"), eq("Failure"), anyString(), contains("duplicate role"));
        verify(roleRepository, times(1)).findByRoleName(roleName);
        verify(roleRepository, never()).save(any(Role.class));
    }

    /**
     * Tests assigning multiple new roles to an existing user.
     *
     * Ensures that roles are added successfully, audit logging occurs,
     * and all repositories are invoked correctly.
     */
    @Test
    void assignRole_Success() {
        Long userId = 1L;
        List<String> roleNames = List.of("ADMIN", "MANAGER");

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleName("ADMIN");

        Role managerRole = new Role();
        managerRole.setId(3L);
        managerRole.setRoleName("MANAGER");

        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(roleRepository.findByRoleName("MANAGER")).thenReturn(Optional.of(managerRole));
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = roleService.assignRoles(userId, roleNames);

        assertNotNull(updatedUser);
        assertTrue(updatedUser.getRoles().contains(adminRole));
        assertTrue(updatedUser.getRoles().contains(managerRole));

        verify(roleRepository, times(1)).findByRoleName("ADMIN");
        verify(roleRepository, times(1)).findByRoleName("MANAGER");
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
        verify(auditService, times(1))
                .recordAction(eq("ROLE_ASSIGNMENT"), eq("Success"), anyString(), contains("Assigned roles"));
    }

    /**
     * Tests assigning a role when the target user does not exist.
     *
     * Expects a RuntimeException and verifies that an audit record is created for failure.
     */
    @Test
    void assignRole_WithNonExistentRole_ShouldThrowException() {

        Long userId = 1L;
        List<String> roleNames = List.of("ADMIN");

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> roleService.assignRoles(userId, roleNames));

        assertEquals("User not found: 1", exception.getMessage());

        verify(auditService, times(1))
                .recordAction(eq("ROLE_ASSIGNMENT"), eq("Failure"), eq("SYSTEM"), contains("User not found"));
    }

    /**
     * Tests assigning a role to a non-existent user ID.
     *
     * Verifies that an exception is thrown and a failure audit is logged.
     */
    @Test
    void assignRole_WithNonExistentUser_ShouldThrowException() {
        Long userId = 999L;
        String roleName = "ADMIN";
        
        List<String> roleNames = List.of("ADMIN");
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> roleService.assignRoles(userId, roleNames));

        assertEquals("User not found: 999", ex.getMessage());
        verify(auditService, times(1))
                .recordAction(eq("ROLE_ASSIGNMENT"), eq("Failure"), anyString(), contains("User not found"));
    }

    /**
     * Tests assigning the same role twice to a user.
     *
     * Ensures duplicate roles are not added and a "No Change" audit is recorded.
     */
    @Test
    void assignRole_SameRoleTwice_ShouldNotDuplicate() {
        Long userId = 1L;
        List<String> roleNames = List.of("ADMIN");

        Role adminRole = new Role();
        adminRole.setId(2L);
        adminRole.setRoleName("ADMIN");

        testUser.getRoles().add(adminRole);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(roleRepository.findByRoleName("ADMIN")).thenReturn(Optional.of(adminRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = roleService.assignRoles(userId, roleNames);

        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(adminRole));

        verify(auditService, times(1))
                .recordAction(eq("ROLE_ASSIGNMENT"), eq("No Change"), eq("SYSTEM"),
                        contains("No new roles were added to user 'testuser'"));
    }

}
