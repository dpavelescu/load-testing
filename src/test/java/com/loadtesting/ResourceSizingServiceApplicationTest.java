package com.loadtesting;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Basic integration test to verify the Spring Boot application context loads correctly.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "app.employee.default-count=10",
    "app.employee.default-string-size=100"
})
class ResourceSizingServiceApplicationTest {

    @Test
    void contextLoads() {
        // This test verifies that the Spring Boot application context loads successfully
        // If this test passes, the basic application setup is correct
    }
}
