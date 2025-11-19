package org.opencms.identity;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Identity Service.
 * 
 * Handles user authentication, authorization, and ACL management.
 * Part of Phase 1 migration from OpenCms monolith.
 */
@SpringBootApplication
public class IdentityServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(IdentityServiceApplication.class, args);
    }
}
