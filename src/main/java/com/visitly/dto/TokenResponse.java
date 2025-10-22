package com.visitly.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response object containing a generated JWT token.
 * 
 * Returned to the client upon successful authentication
 * and used for authorizing subsequent API requests.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {
	
	// The JWT Token issued after a successful login attempt
	private String token;
}
