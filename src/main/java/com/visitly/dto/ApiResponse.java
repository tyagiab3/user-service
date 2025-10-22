package com.visitly.dto;

import java.time.LocalDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * Standardized API response wrapper for all REST endpoints.
 * 
 * Encapsulates the operation status, descriptive message, payload data,
 * and a timestamp for when the response was generated.
 * 
 *
 * @param <T> the type of the response payload
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

	// Indicates operation status: Success or Failure
    private String status;
    
    // Human readable message describing nature of result 
    private String message;
    
    // Response Payload returned by the API
    private T data;
    
    // Timestamp of response creation
    private LocalDateTime timestamp = LocalDateTime.now();
    
    /**
     * Convenience constructor for building success or failure responses.
     *
     * @param isSuccess true if the operation succeeded, false otherwise
     * @param message descriptive message for the response
     * @param data the response payload
     */
    public ApiResponse(boolean isSuccess, String message, T data) {
        this.status = isSuccess ? "success" : "failure";
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
}
