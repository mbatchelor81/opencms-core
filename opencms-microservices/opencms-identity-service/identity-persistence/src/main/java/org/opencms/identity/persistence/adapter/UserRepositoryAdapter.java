package org.opencms.identity.persistence.adapter;

import org.opencms.identity.core.domain.User;
import org.opencms.identity.core.port.UserRepository;
import org.opencms.identity.persistence.entity.UserEntity;
import org.opencms.identity.persistence.repository.JpaUserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Adapter that implements the UserRepository port using JPA.
 * 
 * Follows hexagonal architecture by adapting JPA persistence
 * to the core domain's port interface.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpaRepository;

    public UserRepositoryAdapter(JpaUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jpaRepository.findByEmail(email).map(this::toDomain);
    }

    @Override
    public User save(User user) {
        UserEntity entity = toEntity(user);
        UserEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.existsByUsername(username);
    }

    /**
     * Converts JPA entity to domain model.
     */
    private User toDomain(UserEntity entity) {
        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setFirstName(entity.getFirstName());
        user.setLastName(entity.getLastName());
        user.setPasswordHash(entity.getPasswordHash());
        user.setEnabled(entity.isEnabled());
        user.setCreatedDate(entity.getCreatedDate());
        user.setLastModified(entity.getLastModified());
        user.setCreatedBy(entity.getCreatedBy());
        user.setVersion(entity.getVersion());
        return user;
    }

    /**
     * Converts domain model to JPA entity.
     */
    private UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId());
        entity.setUsername(user.getUsername());
        entity.setEmail(user.getEmail());
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setPasswordHash(user.getPasswordHash());
        entity.setEnabled(user.isEnabled());
        entity.setCreatedDate(user.getCreatedDate());
        entity.setLastModified(user.getLastModified());
        entity.setCreatedBy(user.getCreatedBy());
        entity.setVersion(user.getVersion());
        return entity;
    }
}
