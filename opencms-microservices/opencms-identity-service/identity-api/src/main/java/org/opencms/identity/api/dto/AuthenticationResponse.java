package org.opencms.identity.api.dto;

import java.util.UUID;

/**
 * Response DTO for successful authentication.
 */
public class AuthenticationResponse {

    private UUID userId;
    private String username;
    private String sessionToken;

    // Constructors
    public AuthenticationResponse() {
    }

    public AuthenticationResponse(UUID userId, String username, String sessionToken) {
        this.userId = userId;
        this.username = username;
        this.sessionToken = sessionToken;
    }

    // Getters and Setters
    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getSessionToken() {
        return sessionToken;
    }

    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
}
