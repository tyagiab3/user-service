package com.visitly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object representing a user login request.
 * 
 * Carries the user's credentials for authentication and
 * includes validation rules for input integrity.
 * 
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

	// User's email address for authentication
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // User's password for authentication
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;
}
