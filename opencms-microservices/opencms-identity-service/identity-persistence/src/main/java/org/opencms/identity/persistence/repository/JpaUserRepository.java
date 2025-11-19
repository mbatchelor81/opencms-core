package org.opencms.identity.persistence.repository;

import org.opencms.identity.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entities.
 */
@Repository
public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {

    /**
     * Finds a user by username.
     *
     * @param username the username
     * @return user entity if found
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * Finds a user by email.
     *
     * @param email the email address
     * @return user entity if found
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * Checks if a username exists.
     *
     * @param username the username
     * @return true if exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks if an email exists.
     *
     * @param email the email address
     * @return true if exists
     */
    boolean existsByEmail(String email);
}
