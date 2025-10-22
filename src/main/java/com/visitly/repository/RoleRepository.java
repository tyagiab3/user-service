package com.visitly.repository;

import com.visitly.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for accessing and managing Role entities.
 *
 * Provides standard CRUD operations and custom queries for
 * retrieving roles by their name.
 * 
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	
	/**
     * Finds a role by its name.
     *
     * @param roleName The name of the role to search for
     * @return An Optional containing the matching Role if found
     */
    Optional<Role> findByRoleName(String roleName);
    
}
