package com.visitly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a system role.
 * 
 * Used in API responses to expose basic role information
 * without including related user or permission details.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
	
	// Unique identifier for role
	private Long id;
	
	// Name of the role (eg. ADMIN, USER)
	private String name;
}
