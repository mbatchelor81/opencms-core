package org.opencms.identity.core.service.impl;

import org.opencms.identity.core.domain.User;
import org.opencms.identity.core.port.UserRepository;
import org.opencms.identity.core.service.UserService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of UserService.
 */
@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(12);
    }

    @Override
    public User createUser(User user, String plainPassword) {
        // Validate username doesn't exist
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + user.getUsername());
        }

        // Hash password
        String hashedPassword = passwordEncoder.encode(plainPassword);
        user.setPasswordHash(hashedPassword);

        // Set timestamps
        Instant now = Instant.now();
        user.setCreatedDate(now);
        user.setLastModified(now);
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Override
    public User updateUser(UUID id, User updatedUser) {
        User existingUser = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));

        // Update fields
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getFirstName() != null) {
            existingUser.setFirstName(updatedUser.getFirstName());
        }
        if (updatedUser.getLastName() != null) {
            existingUser.setLastName(updatedUser.getLastName());
        }
        existingUser.setEnabled(updatedUser.isEnabled());
        existingUser.setLastModified(Instant.now());

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }
}
