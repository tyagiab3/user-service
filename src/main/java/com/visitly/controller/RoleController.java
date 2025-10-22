package com.visitly.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.visitly.dto.ApiResponse;
import com.visitly.dto.RoleResponse;
import com.visitly.dto.UserRoleResponse;
import com.visitly.mapper.RoleMapper;
import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.service.RoleService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing user roles within the system.
 * 
 * Provides endpoints for administrators to create new roles and assign
 * existing roles to users. 
 * Access is restricted to users with the ADMIN role.
 * 
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Role Management", description = "Endpoints for creating roles and assigning them to users")
public class RoleController {

	private static final Logger logger = LogManager.getLogger(RoleController.class);
	
    @Autowired
    private RoleService roleService;

    @Autowired
    private RoleMapper roleMapper;
    
    
    /**
     * Creates a new role in the system.
     * 
     * Accessible only by administrators. Ensures role uniqueness before creation.
     * 
     *
     * @param roleName  The name of the role to be created
     * @return a standardized ApiResponse containing the created role details
     */
    @Operation(summary = "Create a new role", description = "Allows admin users to create a new role.")
    @PostMapping("/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@RequestParam String roleName) {
    	logger.info("Attempting to create role: {}", roleName);
        Role role = roleService.createRole(roleName);
        logger.info("Role created successfully: {}", role.getRoleName());
        RoleResponse responseData = roleMapper.toRoleResponse(role);
        return ResponseEntity.ok(new ApiResponse<>(true, "Role created successfully", responseData));
    }

    /**
     * Assigns one or more existing roles to a user.
     * 
     * Only administrators can perform this action. Returns the updated user-role mapping.
     * 
     *
     * @param userId     The ID of the user to whom roles will be assigned
     * @param roleNames  List of role names to assign
     * @return an ApiResponse containing updated user-role relationships
     */
    @Operation(summary = "Assign role(s) to a user", description = "Assigns one or more existing roles to a user by ID.")
    @PostMapping("/users/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserRoleResponse>>> assignRoles(
            @PathVariable Long userId,
            @RequestParam List<String> roleNames) {

        logger.info("Received request to assign roles {} to user ID {}", roleNames, userId);

        User updatedUser = roleService.assignRoles(userId, roleNames);

        List<UserRoleResponse> responseData = updatedUser.getRoles().stream()
                .map(role -> roleMapper.toUserRoleResponse(updatedUser, role))
                .toList();

        logger.info("Roles {} assigned successfully to user {}", roleNames, updatedUser.getUsername());

        return ResponseEntity.ok(
                new ApiResponse<>(true, "Roles assigned successfully", responseData)
        );
    }
}
