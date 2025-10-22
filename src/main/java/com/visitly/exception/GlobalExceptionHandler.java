package com.visitly.exception;

import com.visitly.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

/**
 * Global exception handler for REST controllers.
 *
 * Intercepts and manages common runtime and validation exceptions,
 * returning standardized API responses with consistent formatting
 * and appropriate HTTP status codes.
 * 
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation and bad request errors.
     *
     * @param ex the thrown IllegalArgumentException
     * @param request the current HTTP request
     * @return a standardized failure response with HTTP 400 status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {

        String user = getUser(request);
        String message = ex.getMessage();
        logger.warn("[EXCEPTION] Validation error for {} on {}: {}", user, request.getRequestURI(), message);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>("failure", message, null, LocalDateTime.now()));
    }
    
    
    /**
     * Handles access control violations when a user attempts
     * to access a protected resource without sufficient privileges.
     *
     * @param ex the thrown AccessDeniedException
     * @param request the current HTTP request
     * @return a standardized failure response with HTTP 403 status
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(
            AccessDeniedException ex, HttpServletRequest request) {

        String user = getUser(request);
        logger.error("[EXCEPTION] Access denied for {} on {}: {}", user, request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ApiResponse<>("failure", "Access denied", null, LocalDateTime.now()));
    }

    /**
     * Handles unanticipated runtime exceptions.
     *
     * @param ex the thrown RuntimeException
     * @param request the current HTTP request
     * @return a standardized failure response with HTTP 500 status
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntime(
            RuntimeException ex, HttpServletRequest request) {

        String user = getUser(request);
        logger.error("[EXCEPTION] Runtime exception for {} on {}: {}", user, request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("failure", ex.getMessage(), null, LocalDateTime.now()));
    }

    /**
     * Handles any other unhandled exceptions in the application.
     *
     * @param ex the thrown Exception
     * @param request the current HTTP request
     * @return a standardized failure response with a generic error message
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(
            Exception ex, HttpServletRequest request) {

        String user = getUser(request);
        logger.error("[EXCEPTION] Unhandled exception for {} on {}: {}", user, request.getRequestURI(), ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>("failure", "An unexpected error occurred.", null, LocalDateTime.now()));
    }

    /**
     * Safely retrieves the username from the current request.
     * Defaults to "Anonymous" if the user is not authenticated.
     *
     * @param request the current HTTP request
     * @return the username or "Anonymous" if unauthenticated
     */
    private String getUser(HttpServletRequest request) {
        return (request.getUserPrincipal() != null)
                ? request.getUserPrincipal().getName()
                : "Anonymous";
    }
}
