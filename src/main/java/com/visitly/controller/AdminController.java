package com.visitly.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.visitly.dto.ApiResponse;
import com.visitly.service.AdminService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.Map;

/**
 * REST controller for administrative operations and system-level analytics.
 * 
 * Provides endpoints restricted to admin users for viewing application statistics
 * such as total registered users and recent login activity.
 * 
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin Dashboard", description = "Administrative endpoints for viewing system statistics")
public class AdminController {

    private static final Logger logger = LogManager.getLogger(AdminController.class);

    @Autowired
    private AdminService adminService;

    
    /**
     * Retrieves system-wide statistics for the admin dashboard.
     * 
     * Requires the ADMIN role to access.
     * Returns aggregated data such as user count and recent login timestamps.
     * 
     *
     * @return a standardized ApiResponse containing system metrics
     */
    @Operation(summary = "Get system statistics", description = "Returns total user count and recent login timestamps.")
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAdminStats() {
        logger.info("Fetching system statistics...");
        Map<String, Object> stats = adminService.getSystemStats();
        logger.info("System statistics retrieved successfully: totalUsers={}", stats.get("totalUsers"));
        return ResponseEntity.ok(new ApiResponse<>(true, "System statistics retrieved", stats));
    }
}
