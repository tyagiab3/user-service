package com.visitly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing a user's assigned role.
 *
 * Used in role management responses to show which roles
 * are associated with a given user.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRoleResponse {
	
	// Unique identifier of the user
	private Long userId;
	
	// Username of the associated user
	private String username;
	
	/* Role assigned to the user.
	 * 
	 * This only contains one role as multiple instances 
	 * of this object are used to display multiple roles. 
	 * 
	 */
	private String assignedRole;
}
