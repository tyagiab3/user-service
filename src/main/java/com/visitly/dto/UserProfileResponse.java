package com.visitly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object representing basic user profile information.
 * 
 * Returned after registration, login, or profile retrieval operations.
 * Contains minimal identifying details without exposing sensitive data.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse{
	
		// Unique identifier of user
        private Long id;
        
        // Username that was used to register with
        private String username;
        
        // Email address that was used to register with
        private String email;
}
