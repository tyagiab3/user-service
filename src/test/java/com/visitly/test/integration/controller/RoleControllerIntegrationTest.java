package com.visitly.test.integration.controller;

import com.visitly.controller.RoleController;
import com.visitly.security.JwtUtil;
import com.visitly.service.RoleService;
import com.visitly.mapper.RoleMapper;
import com.visitly.model.Role;
import com.visitly.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

/**
 * Integration tests for the RoleController class.
 *
 * Validates access control and endpoint behavior for role creation
 * and assignment operations, ensuring correct authorization,
 * response structure, and service interaction.
 */
@WebMvcTest(controllers = RoleController.class)
@AutoConfigureMockMvc(addFilters = false)
class RoleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @MockBean
    private RoleMapper roleMapper;

    @MockBean
    private JwtUtil jwtUtil;

    private Role testRole;
    private User testUser;

    // Initializes mock entities used across integration tests.
    @BeforeEach
    void setup() {
        testRole = new Role();
        testRole.setId(1L);
        testRole.setRoleName("MANAGER");

        testUser = new User();
        testUser.setId(100L);
        testUser.setUsername("john_doe");
    }

    /**
     * Tests successful role creation by an authenticated ADMIN user.
     *
     * Verifies that the controller returns a 200 OK response with
     * the correct JSON payload and delegates creation to the service layer.
     */
    @Test
    @DisplayName("POST /api/roles - should allow ADMIN to create role")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testCreateRole_AsAdmin() throws Exception {
        when(roleService.createRole(eq("MANAGER"))).thenReturn(testRole);
        when(roleMapper.toRoleResponse(testRole))
                .thenReturn(new com.visitly.dto.RoleResponse(1L, "MANAGER"));

        mockMvc.perform(post("/api/roles")
                        .param("roleName", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Role created successfully"))
                .andExpect(jsonPath("$.data.name").value("MANAGER"));

        verify(roleService, times(1)).createRole("MANAGER");
    }

    /**
     * Tests access to role creation when unauthenticated.
     *
     * Expects a 5xx error (due to missing authentication) as access
     * is restricted to ADMIN users.
     */
    @Test
    void testCreateRole_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/roles")
                        .param("roleName", "MANAGER")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is5xxServerError());
    }

    /**
     * Tests successful assignment of roles to a user by an ADMIN.
     *
     * Verifies that the endpoint processes request parameters correctly,
     * invokes the service layer, and returns a valid success response.
     */
    @Test
    @DisplayName("POST /api/users/{userId}/roles - should allow ADMIN to assign role")
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testAssignRole_AsAdmin() throws Exception {

        List<String> roles = List.of("MANAGER");
        when(roleService.assignRoles(eq(100L), eq(roles))).thenReturn(testUser);
        when(roleMapper.toUserRoleResponse(eq(testUser), any(Role.class)))
                .thenReturn(new com.visitly.dto.UserRoleResponse(100L, "john_doe", "MANAGER"));

        mockMvc.perform(post("/api/users/{userId}/roles", 100L)
                        .param("roleNames", "MANAGER")   // ✅ use param instead of JSON
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)) // ✅ matches @RequestParam
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Roles assigned successfully"));

        verify(roleService, times(1)).assignRoles(eq(100L), eq(roles));
    }

    /**
     * Tests access restriction for unauthenticated users attempting
     * to assign roles to another user.
     *
     * Expects a 5xx error since authentication is required.
     */
    @Test
    void testAssignRoles_Unauthenticated() throws Exception {
        mockMvc.perform(post("/api/users/{userId}/roles", 100L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[\"MANAGER\"]"))
                .andExpect(status().is5xxServerError());
    }
}
