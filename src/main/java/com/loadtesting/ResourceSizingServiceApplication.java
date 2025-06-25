package com.loadtesting;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot application class for the Resource Sizing Service.
 * This service is designed to simulate employee data with configurable attributes
 * for Kubernetes pod resource sizing through load testing.
 */
@SpringBootApplication
public class ResourceSizingServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ResourceSizingServiceApplication.class, args);
    }
}
