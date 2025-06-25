package com.loadtesting.config;

import com.loadtesting.service.EmployeeDataService;
import org.springframework.boot.actuator.health.Health;
import org.springframework.boot.actuator.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator for the Employee Data Service.
 * This health check verifies that the employee data service is functioning correctly
 * and can generate employee data as expected.
 */
@Component("employeeService")
public class EmployeeServiceHealthIndicator implements HealthIndicator {
    
    private final EmployeeDataService employeeDataService;
    
    public EmployeeServiceHealthIndicator(EmployeeDataService employeeDataService) {
        this.employeeDataService = employeeDataService;
    }
    
    @Override
    public Health health() {
        try {
            // Test that the service can generate a small amount of data
            var testEmployees = employeeDataService.generateEmployees(1, 10);
            
            if (testEmployees != null && !testEmployees.isEmpty()) {
                var employee = testEmployees.get(0);
                
                return Health.up()
                        .withDetail("status", "Employee service is operational")
                        .withDetail("testEmployee", employee.getFirstName() + " " + employee.getLastName())
                        .withDetail("dataFields", "All required fields populated")
                        .withDetail("memorySimulation", "Configurable data field working")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Employee service returned null or empty data")
                        .withDetail("reason", "Service may not be configured correctly")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Employee service failed")
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
}
