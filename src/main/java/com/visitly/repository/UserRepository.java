package com.visitly.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.visitly.model.User;

import java.util.Optional;

/**
 * Repository interface for managing User entities.
 *
 * Provides CRUD operations and custom queries for
 * retrieving and verifying user accounts.
 * 
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	/**
     * Finds a user by their email address.
     *
     * @param email The email of the user to search for
     * @return An Optional containing the matching User if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Checks if a user already exists with the given email address.
     *
     * @param email The email to check for existence
     * @return True if a user with the email exists, false otherwise
     */
    boolean existsByEmail(String email);
}