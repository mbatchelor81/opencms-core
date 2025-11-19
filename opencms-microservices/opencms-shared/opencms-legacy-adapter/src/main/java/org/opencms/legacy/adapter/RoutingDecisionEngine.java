package org.opencms.legacy.adapter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Determines whether to route requests to monolith or microservice.
 * 
 * Supports gradual rollout strategies:
 * - Feature flags
 * - Percentage-based routing
 * - User-based routing
 * - Path-based routing
 */
@Component
public class RoutingDecisionEngine {
    
    @Value("${opencms.migration.identity-service.enabled:false}")
    private boolean identityServiceEnabled;
    
    @Value("${opencms.migration.identity-service.rollout-percentage:0}")
    private int identityServiceRolloutPercentage;
    
    /**
     * Determines if authentication should use the Identity Service.
     * 
     * @return true if Identity Service should handle authentication
     */
    public boolean shouldUseIdentityService() {
        if (!identityServiceEnabled) {
            return false;
        }
        
        // Percentage-based rollout
        if (identityServiceRolloutPercentage >= 100) {
            return true;
        }
        
        // Random percentage routing
        return Math.random() * 100 < identityServiceRolloutPercentage;
    }
    
    /**
     * Determines if a specific user should use the Identity Service.
     * 
     * @param username the username
     * @return true if this user should use Identity Service
     */
    public boolean shouldUseIdentityServiceForUser(String username) {
        if (!identityServiceEnabled) {
            return false;
        }
        
        // Hash-based consistent routing
        int hash = Math.abs(username.hashCode());
        int bucket = hash % 100;
        return bucket < identityServiceRolloutPercentage;
    }
    
    /**
     * Determines if content operations should use the Content Service.
     * 
     * @return true if Content Service should handle VFS operations
     */
    public boolean shouldUseContentService() {
        // TODO: Implement similar logic for Content Service rollout
        return false;
    }
}
