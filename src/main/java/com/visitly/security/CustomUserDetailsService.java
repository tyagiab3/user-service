package com.visitly.security;

import com.visitly.model.User;
import com.visitly.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Custom implementation of Spring Security's UserDetailsService.
 *
 * Loads user details from the database based on the provided email
 * and maps assigned roles to granted authorities for authentication.
 * 
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Loads a user's authentication details by email.
     *
     * @param email The user's email used for lookup
     * @return A UserDetails object containing authentication information
     * @throws UsernameNotFoundException If no user is found with the given email
     * 
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                user.getRoles()
                        .stream()
                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName()))
                        .collect(Collectors.toList())
        );
    }
}
