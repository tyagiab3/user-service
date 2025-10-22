package com.visitly.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * Entity representing the relationship between users and roles.
 *
 * Defines the many-to-many association between User and Role entities,
 * stored in the "user_roles" join table.
 * 
 */
@Entity
@Table(name = "user_roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

	// Unique identifier for the user-role mapping record
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The user associated with this role assignment
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // The role assigned to the user
    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;
}