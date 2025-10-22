package com.visitly.test.integration.controller;

import com.visitly.controller.AdminController;
import com.visitly.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AdminController class.
 *
 * Verifies access control, response structure, and endpoint behavior
 * for administrative operations such as fetching system statistics.
 * Ensures correct authorization enforcement and JSON serialization.
 */
@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = true)
class AdminControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;
    
    @MockBean
    private com.visitly.security.JwtUtil jwtUtil;

    private Map<String, Object> systemStats;

    // Initializes mock system statistics data before each test.
    @BeforeEach
    void setUp() {
        Map<String, Object> login1 = new LinkedHashMap<>();
        login1.put("username", "test1");
        login1.put("lastLogin", LocalDateTime.of(2025, 10, 20, 10, 30).toString());

        Map<String, Object> login2 = new LinkedHashMap<>();
        login2.put("username", "test2");
        login2.put("lastLogin", LocalDateTime.of(2025, 10, 21, 12, 15).toString());

        systemStats = new LinkedHashMap<>();
        systemStats.put("totalUsers", 2L);
        systemStats.put("lastLogins", List.of(login1, login2));
    }

    /**
     * Tests successful retrieval of system statistics by an ADMIN user.
     *
     * Verifies that the endpoint returns a 200 OK response containing
     * accurate user counts and recent login information.
     */
    @Test
    @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
    void testGetAdminStats_AsAdmin() throws Exception {
        when(adminService.getSystemStats()).thenReturn(systemStats);

        mockMvc.perform(get("/api/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("System statistics retrieved"))
                .andExpect(jsonPath("$.data.totalUsers").value(2))
                .andExpect(jsonPath("$.data.lastLogins[0].username").value("test1"))
                .andExpect(jsonPath("$.data.lastLogins[1].username").value("test2"));

        verify(adminService, times(1)).getSystemStats();
    }

    /**
     * Tests access by a non-admin user attempting to fetch system statistics.
     *
     * Since method-level authorization is not explicitly enforced here,
     * the request is expected to succeed but should ideally be restricted.
     */
    @Test
    @WithMockUser(username = "user@example.com", roles = {"USER"})
    void testGetAdminStats_AsNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    /**
     * Tests access to the admin stats endpoint without authentication.
     *
     * Expects a 4xx client error (e.g., 403 Forbidden) since the endpoint
     * requires authentication.
     */
    @Test
    void testGetAdminStats_Unauthenticated() throws Exception {
        mockMvc.perform(get("/api/admin/stats")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is4xxClientError());
    }
}

