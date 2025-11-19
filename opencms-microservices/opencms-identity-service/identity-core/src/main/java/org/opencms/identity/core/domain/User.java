package org.opencms.identity.core.domain;

import java.time.Instant;
import java.util.UUID;

/**
 * Domain model for a user in the OpenCms system.
 * 
 * Maps to CMS_USERS table in the monolith.
 */
public class User {
    
    private UUID id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private boolean enabled;
    private Instant createdDate;
    private Instant lastModified;
    private UUID createdBy;
    
    // Constructors
    public User() {
    }
    
    public User(UUID id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }
    
    // Getters and Setters
    public UUID getId() {
        return id;
    }
    
    public void setId(UUID id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    
    public String getPasswordHash() {
        return passwordHash;
    }
    
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    public Instant getCreatedDate() {
        return createdDate;
    }
    
    public void setCreatedDate(Instant createdDate) {
        this.createdDate = createdDate;
    }
    
    public Instant getLastModified() {
        return lastModified;
    }
    
    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }
    
    public UUID getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }
}
