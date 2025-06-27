package com.loadtesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.loadtesting.config.EmployeeDataProperties;
import com.loadtesting.config.MemorySimulationProperties;

/**
 * Main Spring Boot application class for the Resource Sizing Service.
 * This service is designed to simulate employee data with configurable attributes
 * for Kubernetes pod resource sizing through load testing.
 */
@SpringBootApplication
@EnableConfigurationProperties({EmployeeDataProperties.class, MemorySimulationProperties.class})
public class ResourceSizingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceSizingServiceApplication.class, args);
    }
}
