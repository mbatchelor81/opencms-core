package org.opencms.identity.api.controller;

import org.opencms.identity.api.dto.AuthenticationRequest;
import org.opencms.identity.api.dto.AuthenticationResponse;
import org.opencms.identity.core.domain.User;
import org.opencms.identity.core.service.AuthenticationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

/**
 * REST controller for authentication operations.
 */
@RestController
@RequestMapping("/api/identity")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * Authenticates a user with username and password.
     * POST /api/identity/authenticate
     */
    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticate(
            @RequestBody AuthenticationRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            User user = authenticationService.authenticate(
                request.getUsername(),
                request.getPassword()
            );

            String sessionToken = authenticationService.createSession(
                user,
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
            );

            AuthenticationResponse response = new AuthenticationResponse(
                user.getId(),
                user.getUsername(),
                sessionToken
            );

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
