package org.opencms.legacy.adapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Coordinates dual writes during strangler-fig migration.
 * 
 * Writes are performed to both the monolith (primary) and microservice (shadow).
 * Inconsistencies are logged for later reconciliation.
 */
@Component
public class DualWriteCoordinator {
    
    private static final Logger LOG = LoggerFactory.getLogger(DualWriteCoordinator.class);
    
    /**
     * Executes a write operation asynchronously to the microservice.
     * 
     * @param writeOperation the write operation to execute
     * @return future that completes when write is done
     */
    @Async
    public CompletableFuture<Void> asyncWrite(Runnable writeOperation) {
        return CompletableFuture.runAsync(() -> {
            try {
                writeOperation.run();
                LOG.debug("Shadow write to microservice succeeded");
            } catch (Exception e) {
                LOG.error("Shadow write to microservice failed", e);
                // TODO: Record inconsistency for reconciliation
            }
        });
    }
    
    /**
     * Executes a write operation synchronously to both systems.
     * 
     * @param monolithWrite write to monolith
     * @param microserviceWrite write to microservice
     * @param <T> return type
     * @return result from monolith (primary)
     */
    public <T> T syncDualWrite(
            java.util.function.Supplier<T> monolithWrite,
            java.util.function.Consumer<T> microserviceWrite) {
        
        // Write to monolith (primary)
        T result = monolithWrite.get();
        
        // Write to microservice (shadow)
        try {
            microserviceWrite.accept(result);
        } catch (Exception e) {
            LOG.error("Dual write to microservice failed", e);
            // TODO: Record inconsistency
        }
        
        return result;
    }
}
