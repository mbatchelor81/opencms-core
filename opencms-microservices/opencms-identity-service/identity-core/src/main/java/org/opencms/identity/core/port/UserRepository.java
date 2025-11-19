package org.opencms.identity.core.port;

import org.opencms.identity.core.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Port interface for user persistence.
 * 
 * Defines the contract for user data access without coupling to specific
 * persistence technology (JPA, JDBC, etc.). Follows hexagonal architecture.
 */
public interface UserRepository {
    
    /**
     * Finds a user by ID.
     * 
     * @param id the user ID
     * @return user if found
     */
    Optional<User> findById(UUID id);
    
    /**
     * Finds a user by username.
     * 
     * @param username the username
     * @return user if found
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Finds a user by email.
     * 
     * @param email the email address
     * @return user if found
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Saves a user (create or update).
     * 
     * @param user the user to save
     * @return saved user with generated ID if new
     */
    User save(User user);
    
    /**
     * Deletes a user by ID.
     * 
     * @param id the user ID
     */
    void deleteById(UUID id);
    
    /**
     * Checks if a username exists.
     * 
     * @param username the username
     * @return true if exists
     */
    boolean existsByUsername(String username);
}
