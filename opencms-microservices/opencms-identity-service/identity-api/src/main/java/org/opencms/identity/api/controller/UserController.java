package org.opencms.identity.api.controller;

import org.opencms.identity.api.dto.CreateUserRequest;
import org.opencms.identity.api.dto.UpdateUserRequest;
import org.opencms.identity.api.dto.UserDto;
import org.opencms.identity.core.domain.User;
import org.opencms.identity.core.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/api/identity/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Creates a new user.
     * POST /api/identity/users
     */
    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody CreateUserRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        User created = userService.createUser(user, request.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(created));
    }

    /**
     * Gets a user by ID.
     * GET /api/identity/users/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable("id") UUID id) {
        return userService.findById(id)
            .map(user -> ResponseEntity.ok(toDto(user)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates a user.
     * PUT /api/identity/users/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable("id") UUID id,
            @RequestBody UpdateUserRequest request) {
        
        User updateData = new User();
        updateData.setEmail(request.getEmail());
        updateData.setFirstName(request.getFirstName());
        updateData.setLastName(request.getLastName());
        if (request.getEnabled() != null) {
            updateData.setEnabled(request.getEnabled());
        }

        try {
            User updated = userService.updateUser(id, updateData);
            return ResponseEntity.ok(toDto(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a user.
     * DELETE /api/identity/users/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Converts domain User to UserDto.
     */
    private UserDto toDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEnabled(user.isEnabled());
        dto.setCreatedDate(user.getCreatedDate());
        dto.setLastModified(user.getLastModified());
        return dto;
    }
}
