package com.visitly.repository;

import com.visitly.model.User;
import com.visitly.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for managing UserRole entities.
 *
 * Provides access to user-role associations and supports
 * queries for retrieving roles assigned to specific users.
 * 
 */
@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, Long> {
	
	/**
     * Retrieves all role assignments for the given user.
     *
     * @param user The user whose roles are to be retrieved
     * @return A list of UserRole records associated with the user
     */
    List<UserRole> findByUser(User user);
}
