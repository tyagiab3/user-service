package com.visitly.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.visitly.model.Role;
import com.visitly.model.User;
import com.visitly.repository.RoleRepository;
import com.visitly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Service layer responsible for managing system roles and user-role assignments.
 *
 * Handles creation of new roles, assignment of roles to users, and ensures that
 * all role-related actions are logged through the audit logging service.
 * 
 */
@Service
public class RoleService {

    private static final Logger logger = LogManager.getLogger(RoleService.class);

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Retrieves the email address of the currently authenticated user.
     * Returns "SYSTEM" if no user is authenticated.
     *
     * @return The email of the authenticated user or "SYSTEM" if unauthenticated
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }

    /**
     * Creates a new role in the system if it does not already exist.
     *
     * Records the creation attempt in the audit log, including both
     * successful and duplicate role creation events.
     *
     * @param roleName The name of the role to be created
     * @return The newly created Role entity
     * @throws RuntimeException If a role with the same name already exists
     */
    public Role createRole(String roleName) {
        String adminEmail = getCurrentUserEmail();
        logger.info("Creating role: {} by {}", roleName, adminEmail);

        Optional<Role> existing = roleRepository.findByRoleName(roleName);
        if (existing.isPresent()) {
            String msg = "Attempt to create duplicate role '" + roleName + "'";
            logger.warn("{} by {}", msg, adminEmail);
            auditService.recordAction("ROLE_CREATION", "Failure", adminEmail, msg);
            throw new RuntimeException("Role already exists");
        }

        Role role = new Role();
        role.setRoleName(roleName);
        Role saved = roleRepository.save(role);

        String msg = "Role '" + saved.getRoleName() + "' created successfully";
        logger.info("{} by {}", msg, adminEmail);
        auditService.recordAction("ROLE_CREATION", "Success", adminEmail, msg);
        return saved;
    }

    /**
     * Assigns one or more roles to a user.
     *
     * Fetches the user by ID, retrieves each role by name, and updates
     * the user's assigned roles. All role assignment events are logged
     * via the audit service.
     *
     * @param userId The ID of the user receiving the roles
     * @param roleNames A list of role names to assign to the user
     * @return The updated User entity with newly assigned roles
     * @throws RuntimeException If the user or any specified role cannot be found
     */
    public User assignRoles(Long userId, List<String> roleNames) {
        String adminEmail = getCurrentUserEmail();
        logger.info("Assigning roles {} to user ID {} by {}", roleNames, userId, adminEmail);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> {
                    String msg = "User not found: " + userId;
                    auditService.recordAction("ROLE_ASSIGNMENT", "Failure", adminEmail, msg);
                    return new RuntimeException(msg);
                });

        List<Role> rolesToAdd = roleNames.stream()
                .map(roleName -> roleRepository.findByRoleName(roleName)
                        .orElseThrow(() -> {
                            String msg = "Role not found: " + roleName;
                            auditService.recordAction("ROLE_ASSIGNMENT", "Failure", adminEmail, msg);
                            throw new RuntimeException(msg);
                        })
                )
                .toList();

        Set<Role> currentRoles = user.getRoles();
        int beforeCount = currentRoles.size();
        currentRoles.addAll(rolesToAdd);

        User updated = userRepository.save(user);
        int addedCount = updated.getRoles().size() - beforeCount;

        if (addedCount > 0) {
            String msg = String.format("Assigned roles %s to user '%s'", roleNames, updated.getUsername());
            logger.info("{} by {}", msg, adminEmail);
            auditService.recordAction("ROLE_ASSIGNMENT", "Success", adminEmail, msg);
        } else {
            String msg = String.format("No new roles were added to user '%s' (already had them)", updated.getUsername());
            logger.info("{} by {}", msg, adminEmail);
            auditService.recordAction("ROLE_ASSIGNMENT", "No Change", adminEmail, msg);
        }

        return updated;
    }
}
