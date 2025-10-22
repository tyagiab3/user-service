package com.visitly.test.unit.controller;

import com.visitly.controller.AdminController;
import com.visitly.dto.ApiResponse;
import com.visitly.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the AdminController class.
 *
 * Validates the behavior of admin-related endpoints that fetch
 * system statistics, ensuring correct response structure,
 * data integrity, and error propagation.
 */
@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AdminController adminController;

    private Map<String, Object> mockStats;
    private List<Map<String, Object>> lastLogins;

    // Initializes mock system statistics and user login data before each test.
    @BeforeEach
    void setUp() {
        Map<String, Object> login1 = new HashMap<>();
        login1.put("username", "user1");
        login1.put("lastLogin", LocalDateTime.of(2024, 1, 10, 12, 0));

        Map<String, Object> login2 = new HashMap<>();
        login2.put("username", "user2");
        login2.put("lastLogin", LocalDateTime.of(2024, 1, 11, 15, 30));

        lastLogins = Arrays.asList(login1, login2);

        mockStats = new LinkedHashMap<>();
        mockStats.put("totalUsers", 2L);
        mockStats.put("lastLogins", lastLogins);
    }

    /**
     * Tests successful retrieval of system statistics.
     *
     * Verifies that the response includes correct total user count,
     * recent login data, and an HTTP 200 status.
     */
    @Test
    void getAdminStats_ShouldReturnOkResponseWithCorrectData() {
        when(adminService.getSystemStats()).thenReturn(mockStats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = adminController.getAdminStats();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<Map<String, Object>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals("System statistics retrieved", apiResponse.getMessage());
        assertEquals(2L, apiResponse.getData().get("totalUsers"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> resultLogins = (List<Map<String, Object>>) apiResponse.getData().get("lastLogins");
        assertEquals(2, resultLogins.size());
        assertEquals("user1", resultLogins.get(0).get("username"));
        assertEquals("user2", resultLogins.get(1).get("username"));

        verify(adminService, times(1)).getSystemStats();
    }

    /**
     * Tests behavior when no users exist in the system.
     *
     * Ensures the controller correctly handles zero counts
     * and returns an empty login list.
     */
    @Test
    void getAdminStats_WhenNoUsers_ShouldReturnZeroCount() {

        Map<String, Object> emptyStats = new LinkedHashMap<>();
        emptyStats.put("totalUsers", 0L);
        emptyStats.put("lastLogins", Collections.emptyList());
        when(adminService.getSystemStats()).thenReturn(emptyStats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = adminController.getAdminStats();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());

        ApiResponse<Map<String, Object>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        assertEquals(0L, apiResponse.getData().get("totalUsers"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> lastLogins = (List<Map<String, Object>>) apiResponse.getData().get("lastLogins");
        assertTrue(lastLogins.isEmpty());

        verify(adminService, times(1)).getSystemStats();
    }

    /**
     * Tests that the controller preserves LinkedHashMap order in responses.
     *
     * Ensures the response keys retain insertion order for predictable structure.
     */
    @Test
    void getAdminStats_ShouldPreserveLinkedHashMapOrder() {
        when(adminService.getSystemStats()).thenReturn(mockStats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = adminController.getAdminStats();

        ApiResponse<Map<String, Object>> apiResponse = response.getBody();
        assertNotNull(apiResponse);
        Map<String, Object> stats = apiResponse.getData();

        assertTrue(stats instanceof LinkedHashMap);
        List<String> keys = new ArrayList<>(stats.keySet());
        assertEquals("totalUsers", keys.get(0));
        assertEquals("lastLogins", keys.get(1));
    }

    /**
     * Tests behavior when the AdminService throws a runtime exception.
     *
     * Verifies that exceptions from the service layer are correctly propagated.
     */
    @Test
    void getAdminStats_WhenServiceThrowsException_ShouldPropagateException() {
        when(adminService.getSystemStats()).thenThrow(new RuntimeException("Service error"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> adminController.getAdminStats());
        assertEquals("Service error", ex.getMessage());
        verify(adminService, times(1)).getSystemStats();
    }

    /**
     * Validates overall response structure.
     *
     * Ensures that the returned API response contains
     * expected fields: totalUsers and lastLogins.
     */
    @Test
    void getAdminStats_ResponseStructure_ShouldBeValid() {
        when(adminService.getSystemStats()).thenReturn(mockStats);

        ResponseEntity<ApiResponse<Map<String, Object>>> response = adminController.getAdminStats();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<Map<String, Object>> apiResponse = response.getBody();

        assertNotNull(apiResponse);
        assertTrue(apiResponse.getData().containsKey("totalUsers"));
        assertTrue(apiResponse.getData().containsKey("lastLogins"));
        verify(adminService, times(1)).getSystemStats();
    }
}
