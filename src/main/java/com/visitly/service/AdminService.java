package com.visitly.service;

import com.visitly.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service layer responsible for administrative operations and system-level analytics.
 *
 * Provides functionality for retrieving application statistics such as
 * total registered users and recent login activity. Actions are logged
 * through the audit logging mechanism.
 * 
 */
@Service
public class AdminService {

    private static final Logger logger = LogManager.getLogger(AdminService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Retrieves the email address of the currently authenticated user.
     * Returns "SYSTEM" if the user is not authenticated.
     *
     * @return The email of the current user or "SYSTEM" if unauthenticated
     */
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "SYSTEM";
    }

    /**
     * Retrieves overall system statistics for the admin dashboard.
     *
     * Includes total user count and a list of usernames with their most recent login times.
     * Records a corresponding audit log entry upon successful retrieval.
     *
     * @return A map containing total user count and recent login details
     */
    public Map<String, Object> getSystemStats() {
        String adminEmail = getCurrentUserEmail();
        logger.info("Admin '{}' is fetching system statistics...", adminEmail);

        long totalUsers = userRepository.count();

        // Collect usernames and last login timestamps for all users
        List<Map<String, Object>> lastLogins = userRepository.findAll().stream()
                .map(u -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("username", u.getUsername());
                    m.put("lastLogin", u.getLastLogin());
                    return m;
                })
                .collect(Collectors.toList());

        // Prepare system statistics payload
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("lastLogins", lastLogins);
        
        // Record the admin’s action in the audit log
        auditService.recordAction(
                "ADMIN_STATS_VIEW",
                "Success",
                adminEmail,
                "Viewed system statistics (totalUsers=" + totalUsers + ")"
        );
        logger.info("System statistics retrieved successfully by {}", adminEmail);

        return stats;
    }
}
