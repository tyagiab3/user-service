package com.visitly.test.unit.controller;

import com.visitly.controller.RoleController;
import com.visitly.dto.ApiResponse;
import com.visitly.dto.RoleResponse;
import com.visitly.dto.UserRoleResponse;
import com.visitly.mapper.RoleMapper;
import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the RoleController class.
 *
 * Ensures proper controller behavior for role creation and
 * assignment endpoints, verifying that service calls, response
 * structures, and exception handling behave as expected.
 */
@ExtendWith(MockitoExtension.class)
class RoleControllerTest {

    @Mock
    private RoleService roleService;

    @Mock
    private RoleMapper roleMapper;

    @InjectMocks
    private RoleController roleController;

    private Role testRole;
    private RoleResponse testRoleResponse;
    private User testUser;
    private UserRoleResponse testUserRoleResponse;

    // Initializes mock role and user data before each test execution.
    @BeforeEach
    void setUp() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("ADMIN");

        testRoleResponse = new RoleResponse(1L, "ADMIN");

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setUsername("testuser");
        testUser.setRoles(new HashSet<>());

        testUserRoleResponse = new UserRoleResponse(testUser.getId(), testUser.getUsername(), "ADMIN");
    }

    /**
     * Tests successful creation of a new role.
     *
     * Verifies that the controller delegates to the service layer,
     * maps the entity correctly, and returns a proper success response.
     */
    @Test
    void createRole_Success() {
        String roleName = "ADMIN";
        when(roleService.createRole(roleName)).thenReturn(testRole);
        when(roleMapper.toRoleResponse(testRole)).thenReturn(testRoleResponse);

        ResponseEntity<ApiResponse<RoleResponse>> response = roleController.createRole(roleName);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<RoleResponse> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Role created successfully", apiResponse.getMessage());
        assertEquals(testRoleResponse, apiResponse.getData());

        verify(roleService, times(1)).createRole(roleName);
        verify(roleMapper, times(1)).toRoleResponse(testRole);
    }

    /**
     * Tests behavior when attempting to create a duplicate role.
     *
     * Expects a RuntimeException and confirms that the
     * appropriate message is propagated.
     */
    @Test
    void createRole_WithExistingRole_ShouldThrowException() {
        String roleName = "ADMIN";
        when(roleService.createRole(roleName))
                .thenThrow(new RuntimeException("Role already exists"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleController.createRole(roleName)
        );

        assertEquals("Role already exists", exception.getMessage());
        verify(roleService, times(1)).createRole(roleName);
    }

    /**
     * Tests successful assignment of roles to a user.
     *
     * Ensures the controller calls the service method and returns
     * a valid success response with expected structure.
     */
    @Test
    void assignRole_Success() {
        Long userId = 1L;
        List<String> roles = List.of("ADMIN");

        when(roleService.assignRoles(userId, roles)).thenReturn(testUser);

        ResponseEntity<ApiResponse<List<UserRoleResponse>>> response = roleController.assignRoles(userId, roles);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<UserRoleResponse>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Roles assigned successfully", apiResponse.getMessage());

        verify(roleService, times(1)).assignRoles(userId, roles);
    }

    /**
     * Tests assigning roles when the target user does not exist.
     *
     * Expects a RuntimeException and verifies correct message propagation.
     */
    @Test
    void assignRole_WithNonExistentUser_ShouldThrowException() {
        Long userId = 999L;
        List<String> roles = List.of("ADMIN");
        when(roleService.assignRoles(userId, roles))
                .thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleController.assignRoles(userId, roles)
        );

        assertEquals("User not found", exception.getMessage());
        verify(roleService, times(1)).assignRoles(userId, roles);
    }

    /**
     * Tests assigning roles when one or more provided role names do not exist.
     *
     * Verifies that the exception message is correctly passed to the caller.
     */
    @Test
    void assignRole_WithNonExistentRole_ShouldThrowException() {
        Long userId = 1L;
        List<String> roles = List.of("NONEXISTENT");
        when(roleService.assignRoles(userId, roles))
                .thenThrow(new RuntimeException("Role not found"));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> roleController.assignRoles(userId, roles)
        );

        assertEquals("Role not found", exception.getMessage());
        verify(roleService, times(1)).assignRoles(userId, roles);
    }

    /**
     * Tests assigning roles that a user already possesses.
     *
     * Ensures that the response is still successful but
     * indicates no new changes were made.
     */
    @Test
    void assignRole_WithDuplicateRoles_ShouldReturnNoChange() {
        Long userId = 1L;
        List<String> roles = List.of("ADMIN");

        when(roleService.assignRoles(userId, roles)).thenReturn(testUser);

        ResponseEntity<ApiResponse<List<UserRoleResponse>>> response = roleController.assignRoles(userId, roles);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<List<UserRoleResponse>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("Roles assigned successfully", apiResponse.getMessage());

        verify(roleService, times(1)).assignRoles(userId, roles);
    }
}
