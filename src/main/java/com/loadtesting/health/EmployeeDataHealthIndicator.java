package com.loadtesting.health;

import com.loadtesting.service.EmployeeDataService;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Simple health indicator for Employee Data Service
 */
@Component
public class EmployeeDataHealthIndicator implements HealthIndicator {
    
    private final EmployeeDataService employeeDataService;
    
    public EmployeeDataHealthIndicator(EmployeeDataService employeeDataService) {
        this.employeeDataService = employeeDataService;
    }
    
    @Override
    public Health health() {
        try {
            // Simple test - generate one employee
            var employees = employeeDataService.generateEmployees(1, 50);
            
            if (employees != null && !employees.isEmpty()) {
                return Health.up()
                        .withDetail("service", "EmployeeDataService")
                        .withDetail("status", "operational")
                        .withDetail("test", "successfully generated employee data")
                        .build();
            } else {
                return Health.down()
                        .withDetail("service", "EmployeeDataService")
                        .withDetail("status", "failed")
                        .withDetail("reason", "no data generated")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("service", "EmployeeDataService")
                    .withDetail("status", "error")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
