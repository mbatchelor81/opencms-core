package org.opencms.identity.core.service;

import org.opencms.identity.core.domain.User;

import java.util.Optional;

/**
 * Core authentication service interface.
 * 
 * Handles user authentication, session management, and credential validation.
 * Replaces CmsSecurityManager authentication logic from the monolith.
 */
public interface AuthenticationService {
    
    /**
     * Authenticates a user with username and password.
     * 
     * @param username the username
     * @param password the plain-text password
     * @return authenticated user
     * @throws AuthenticationException if authentication fails
     */
    User authenticate(String username, String password);
    
    /**
     * Creates a new session for an authenticated user.
     * 
     * @param user the authenticated user
     * @param ipAddress the client IP address
     * @param userAgent the client user agent
     * @return session token
     */
    String createSession(User user, String ipAddress, String userAgent);
    
    /**
     * Validates a session token and returns the associated user.
     * 
     * @param sessionToken the session token
     * @return user if session is valid, empty otherwise
     */
    Optional<User> validateSession(String sessionToken);
    
    /**
     * Invalidates a session.
     * 
     * @param sessionToken the session token to invalidate
     */
    void invalidateSession(String sessionToken);
    
    /**
     * Validates a password against the user's stored hash.
     * 
     * @param user the user
     * @param password the plain-text password
     * @return true if password matches
     */
    boolean validatePassword(User user, String password);
    
    /**
     * Hashes a plain-text password for storage.
     * 
     * @param password the plain-text password
     * @return hashed password
     */
    String hashPassword(String password);
}
