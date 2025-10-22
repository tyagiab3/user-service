package com.visitly.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data transfer object for new user registration requests.
 * 
 * Contains user-provided information required to create an account,
 * with validation to ensure proper formatting and completeness.
 * 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterUserRequest {

	// Username chosen by user during registration
    @NotBlank(message = "Username is required")
    private String username;

    // User's email address used for account creation and authentication
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    // User's password used for account creation and authentication
    // Password validated for minimum and maximum length
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 50, message = "Password must be between 6 and 50 characters")
    private String password;

}
