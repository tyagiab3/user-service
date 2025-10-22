package com.visitly.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing a user role within the system.
 *
 * Defines role-based access levels such as ADMIN or USER,
 * used to authorize actions throughout the application.
 * 
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Role {
	
	// Unique identifier for the role.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Name of the role (eg., ADMIN, USER)
    @Column(name = "role_name", nullable = false, unique = true)
    private String roleName;
}