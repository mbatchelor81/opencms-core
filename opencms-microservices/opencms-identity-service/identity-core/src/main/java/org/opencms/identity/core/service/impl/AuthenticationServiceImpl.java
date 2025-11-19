package org.opencms.identity.core.service.impl;

import org.opencms.identity.core.domain.User;
import org.opencms.identity.core.port.UserRepository;
import org.opencms.identity.core.service.AuthenticationService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of AuthenticationService.
 * 
 * Handles user authentication and session management.
 * In Phase 1, uses simple in-memory session tokens.
 * Future phases will integrate Redis for distributed sessions.
 */
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthenticationServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!user.isEnabled()) {
            throw new IllegalArgumentException("User account is disabled");
        }

        if (!validatePassword(user, password)) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return user;
    }

    @Override
    public String createSession(User user, String ipAddress, String userAgent) {
        // Simple session token for Phase 1
        // TODO: Integrate Redis for distributed session storage in future phases
        return UUID.randomUUID().toString();
    }

    @Override
    public Optional<User> validateSession(String sessionToken) {
        // Placeholder for Phase 1
        // TODO: Implement Redis-based session validation
        return Optional.empty();
    }

    @Override
    public void invalidateSession(String sessionToken) {
        // Placeholder for Phase 1
        // TODO: Implement Redis-based session invalidation
    }

    @Override
    public boolean validatePassword(User user, String password) {
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}
