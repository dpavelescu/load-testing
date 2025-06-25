package com.loadtesting.service;

import com.loadtesting.config.EmployeeDataProperties;
import com.loadtesting.model.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EmployeeDataService to verify employee data generation
 * with configurable attributes and memory consumption scenarios.
 */
class EmployeeDataServiceTest {
    
    private EmployeeDataService employeeDataService;
    private EmployeeDataProperties properties;
    
    @BeforeEach
    void setUp() {
        properties = new EmployeeDataProperties();
        properties.setDefaultCount(10);
        properties.setMaxCount(100);
        properties.setDefaultStringSize(100);
        properties.setMaxStringSize(1000);
        properties.setMinStringSize(10);
        
        employeeDataService = new EmployeeDataService(properties);
    }
    
    @Test
    void testGenerateEmployeesWithDefaultSettings() {
        List<Employee> employees = employeeDataService.generateEmployees();
        
        assertNotNull(employees);
        assertEquals(properties.getDefaultCount(), employees.size());
        
        // Verify each employee has all required fields
        for (Employee employee : employees) {
            assertNotNull(employee.getId());
            assertNotNull(employee.getFirstName());
            assertNotNull(employee.getLastName());
            assertNotNull(employee.getEmail());
            assertNotNull(employee.getPosition());
            assertNotNull(employee.getDepartment());
            assertNotNull(employee.getSalary());
            assertNotNull(employee.getHireDate());
            assertNotNull(employee.getConfigurableData());
            
            // Verify configurable data has default size
            assertEquals(properties.getDefaultStringSize(), employee.getConfigurableData().length());
            
            // Verify email format
            assertTrue(employee.getEmail().contains("@company.com"));
            assertTrue(employee.getEmail().contains("."));
        }
    }
    
    @Test
    void testGenerateEmployeesWithCustomCount() {
        int customCount = 25;
        List<Employee> employees = employeeDataService.generateEmployees(customCount);
        
        assertNotNull(employees);
        assertEquals(customCount, employees.size());
        
        // Verify unique IDs
        long distinctIds = employees.stream().mapToLong(Employee::getId).distinct().count();
        assertEquals(customCount, distinctIds);
    }
    
    @Test
    void testGenerateEmployeesWithCustomStringSize() {
        int customCount = 5;
        int customStringSize = 200;
        List<Employee> employees = employeeDataService.generateEmployees(customCount, customStringSize);
        
        assertNotNull(employees);
        assertEquals(customCount, employees.size());
        
        // Verify all employees have the custom string size
        for (Employee employee : employees) {
            assertEquals(customStringSize, employee.getConfigurableData().length());
        }
    }
    
    @Test
    void testGenerateEmployeesRespectMaxLimits() {
        int exceedingCount = properties.getMaxCount() + 50;
        int exceedingStringSize = properties.getMaxStringSize() + 1000;
        
        List<Employee> employees = employeeDataService.generateEmployees(exceedingCount, exceedingStringSize);
        
        // Should be limited to max values
        assertEquals(properties.getMaxCount(), employees.size());
        for (Employee employee : employees) {
            assertEquals(properties.getMaxStringSize(), employee.getConfigurableData().length());
        }
    }
    
    @Test
    void testGenerateEmployeesRespectMinLimits() {
        int belowMinCount = 0;
        int belowMinStringSize = properties.getMinStringSize() - 5;
        
        List<Employee> employees = employeeDataService.generateEmployees(belowMinCount, belowMinStringSize);
        
        // Should be adjusted to minimum values
        assertEquals(1, employees.size()); // Minimum count is 1
        for (Employee employee : employees) {
            assertEquals(properties.getMinStringSize(), employee.getConfigurableData().length());
        }
    }
    
    @Test
    void testGenerateSingleEmployee() {
        Long testId = 42L;
        int testStringSize = 150;
        
        Employee employee = employeeDataService.generateSingleEmployee(testId, testStringSize);
        
        assertNotNull(employee);
        assertEquals(testId, employee.getId());
        assertEquals(testStringSize, employee.getConfigurableData().length());
        assertNotNull(employee.getFirstName());
        assertNotNull(employee.getLastName());
        assertNotNull(employee.getEmail());
        assertTrue(employee.getSalary() >= 40000.0);
        assertTrue(employee.getSalary() <= 150000.0);
    }
    
    @Test
    void testCalculateTotalMemoryConsumption() {
        List<Employee> employees = employeeDataService.generateEmployees(5, 100);
        
        long totalMemory = employeeDataService.calculateTotalMemoryConsumption(employees);
        
        assertTrue(totalMemory > 0);
        
        // Verify it matches sum of individual employee memory sizes
        long expectedTotal = employees.stream()
                .mapToLong(Employee::getApproximateMemorySize)
                .sum();
        assertEquals(expectedTotal, totalMemory);
    }
    
    @Test
    void testGetDataStats() {
        int count = 10;
        int stringSize = 200;
        List<Employee> employees = employeeDataService.generateEmployees(count, stringSize);
        
        EmployeeDataService.EmployeeDataStats stats = employeeDataService.getDataStats(employees);
        
        assertNotNull(stats);
        assertEquals(count, stats.getCount());
        assertEquals(stringSize, stats.getAvgStringSize());
        assertTrue(stats.getTotalMemoryBytes() > 0);
        assertTrue(stats.getAvgMemoryPerEmployeeBytes() > 0);
        
        // Verify average calculation
        assertEquals(stats.getTotalMemoryBytes() / count, stats.getAvgMemoryPerEmployeeBytes());
    }
    
    @Test
    void testGetDataStatsWithEmptyList() {
        List<Employee> emptyList = List.of();
        
        EmployeeDataService.EmployeeDataStats stats = employeeDataService.getDataStats(emptyList);
        
        assertNotNull(stats);
        assertEquals(0, stats.getCount());
        assertEquals(0, stats.getTotalMemoryBytes());
        assertEquals(0, stats.getAvgMemoryPerEmployeeBytes());
        assertEquals(0, stats.getAvgStringSize());
    }
    
    @Test
    void testMemoryConsumptionScaling() {
        // Test that larger string sizes result in proportionally larger memory consumption
        List<Employee> smallDataEmployees = employeeDataService.generateEmployees(10, 100);
        List<Employee> largeDataEmployees = employeeDataService.generateEmployees(10, 1000);
        
        long smallMemory = employeeDataService.calculateTotalMemoryConsumption(smallDataEmployees);
        long largeMemory = employeeDataService.calculateTotalMemoryConsumption(largeDataEmployees);
        
        // Large data should consume significantly more memory
        assertTrue(largeMemory > smallMemory);
        
        // The ratio should be roughly proportional to string size ratio (with some base overhead)
        double memoryRatio = (double) largeMemory / smallMemory;
        assertTrue(memoryRatio > 5); // Should be much larger, though not exactly 10x due to base overhead
    }
}
