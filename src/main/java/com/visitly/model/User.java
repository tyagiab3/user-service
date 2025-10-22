package com.visitly.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a registered user in the system.
 *
 * Stores authentication credentials, contact information,
 * and associated roles for access control.
 * 
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

	// Unique identifier for the user
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Username chosen by the user, used for identification within the system
    @Column(nullable = false, unique = true)
    private String username;

    // Email address associated with the user account
    @Column(nullable = false, unique = true)
    private String email;

    // Hashed password used for authentication
    @Column(name = "password_hash", nullable = false)
    private String password;
    
    // Timestamp of the user's most recent successful login
    @Column(name = "last_login")
    private LocalDateTime lastLogin;
    
    // Roles assigned to the user, defining access permissions
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
    
}
