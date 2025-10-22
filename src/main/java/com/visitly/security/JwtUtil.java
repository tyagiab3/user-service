package com.visitly.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Claims;


import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

/**
 * Utility class for generating and validating JSON Web Tokens (JWT).
 *
 * Handles token creation, extraction of claims, role retrieval,
 * and validation of token integrity and expiration.
 * 
 */
@Component
public class JwtUtil {

	// Secret key used for signing JWT tokens
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    
    // Token expiration time in milliseconds (1 hour)
    private final long expirationTime = 1000 * 60 * 60;

    
    /**
     * Generates a JWT token containing the user's email and roles.
     *
     * @param email The user's email to include as the subject
     * @param roles The list of roles to embed in the token claims
     * @return A signed JWT token string
     */
    public String generateToken(String email, List<String> roles) {
        return Jwts.builder()
                .setSubject(email)
                .claim("roles", roles)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Extracts the username (email) from a given token.
     *
     * @param token The JWT token to parse
     * @return The email extracted from the token's subject claim
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extracts user roles embedded within the token.
     *
     * @param token The JWT token to parse
     * @return A list of role names associated with the user
     */
    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        return (List<String>) extractAllClaims(token).get("roles");
    }

    /**
     * Extracts a specific claim from the token using a resolver function.
     *
     * @param token The JWT token to parse
     * @param claimsResolver The function used to resolve a specific claim
     * @param <T> The type of the claim value
     * @return The extracted claim value
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses the token and retrieves all claims.
     *
     * @param token The JWT token to parse
     * @return The Claims object containing all token data
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Checks whether the given token has expired.
     *
     * @param token The JWT token to check
     * @return True if the token has expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }

    /**
     * Validates a token by verifying its subject and expiration status.
     *
     * @param token The JWT token to validate
     * @param email The expected email associated with the token
     * @return True if the token is valid, false otherwise
     */
    public boolean validateToken(String token, String email) {
        final String extractedEmail = extractUsername(token);
        return (extractedEmail.equals(email) && !isTokenExpired(token));
    }
    
    
}
