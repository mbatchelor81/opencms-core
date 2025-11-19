package org.opencms.identity.core.service;

import org.opencms.identity.core.domain.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Core user management service interface.
 */
public interface UserService {

    /**
     * Creates a new user.
     *
     * @param user the user to create
     * @param plainPassword the plain-text password
     * @return created user
     */
    User createUser(User user, String plainPassword);

    /**
     * Updates an existing user.
     *
     * @param id the user ID
     * @param user the updated user data
     * @return updated user
     */
    User updateUser(UUID id, User user);

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
     * Deletes a user by ID.
     *
     * @param id the user ID
     */
    void deleteUser(UUID id);
}
