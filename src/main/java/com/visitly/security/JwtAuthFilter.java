package com.visitly.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.visitly.dto.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Filter responsible for validating JWT tokens on incoming requests.
 *
 * Extracts and verifies the JWT from the Authorization header, sets the
 * authentication context for valid tokens, and handles invalid or expired tokens.
 * 
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Processes each incoming HTTP request to validate the JWT token.
     * 
     * If a valid token is found, the user's authentication is established
     * in the security context. Invalid or expired tokens result in an
     * unauthorized response.
     *
     * @param request The incoming HTTP request
     * @param response The outgoing HTTP response
     * @param chain The filter chain to continue request processing
     * @throws ServletException If servlet-specific errors occur
     * @throws IOException If input/output errors occur
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // Skip processing if no Bearer token is provided
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        String username;

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (ExpiredJwtException e) {
            writeErrorResponse(response, "Token has expired");
            return;
        } catch (SignatureException e) {
            writeErrorResponse(response, "Invalid JWT signature");
            return;
        } catch (MalformedJwtException e) {
            writeErrorResponse(response, "Malformed JWT token");
            return;
        } catch (Exception e) {
            writeErrorResponse(response, "Invalid or unsupported token");
            return;
        }

        // Authenticate the user if the token is valid and no authentication exists
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            List<String> roles = jwtUtil.extractRoles(jwt);
            List<SimpleGrantedAuthority> authorities = roles.stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        chain.doFilter(request, response);
    }

    /**
     * Writes an unauthorized JSON response for invalid or expired tokens.
     *
     * @param response The HTTP response to write to
     * @param message The error message to include in the response
     * @throws IOException If writing to the response fails
     */
    private void writeErrorResponse(HttpServletResponse response, String message) throws IOException {
        ApiResponse<Void> apiResponse = new ApiResponse<>(false, "Unauthorized: " + message, null);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json");
        response.getWriter().write(mapper.writeValueAsString(apiResponse));
        response.getWriter().flush();
    }
}
