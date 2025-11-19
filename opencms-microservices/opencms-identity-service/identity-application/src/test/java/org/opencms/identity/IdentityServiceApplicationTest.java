package org.opencms.identity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Basic integration test to verify Spring Boot application starts correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class IdentityServiceApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the Spring application context loads successfully
    }
}
