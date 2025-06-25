package com.loadtesting.service;

import com.loadtesting.config.EmployeeDataProperties;
import com.loadtesting.model.Employee;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Service class responsible for generating employee data with configurable attributes.
 * This service can generate different amounts of data with variable string field sizes
 * to simulate different memory consumption scenarios for load testing.
 */
@Service
public class EmployeeDataService {
    
    private final EmployeeDataProperties properties;
    private final Random random;
    
    // Sample data arrays for generating realistic employee information
    private static final String[] FIRST_NAMES = {
        "John", "Jane", "Michael", "Sarah", "David", "Lisa", "Robert", "Emily",
        "James", "Jessica", "William", "Ashley", "Richard", "Amanda", "Charles", "Melissa"
    };
    
    private static final String[] LAST_NAMES = {
        "Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
        "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas"
    };
    
    private static final String[] DEPARTMENTS = {
        "Engineering", "Marketing", "Sales", "Human Resources", "Finance", 
        "Operations", "Customer Service", "IT", "Legal", "Research"
    };
    
    private static final String[] POSITIONS = {
        "Software Engineer", "Senior Developer", "Manager", "Director", "Analyst",
        "Specialist", "Coordinator", "Associate", "Lead", "Principal", "Vice President"
    };
      public EmployeeDataService(EmployeeDataProperties properties) {
        this.properties = properties;
        this.random = new Random();
    }
    
    /**
     * Generate a list of employees with default configuration
     */
    public List<Employee> generateEmployees() {
        return generateEmployees(properties.getDefaultCount(), properties.getDefaultStringSize());
    }
    
    /**
     * Generate a list of employees with specified count and default string size
     */
    public List<Employee> generateEmployees(int count) {
        return generateEmployees(count, properties.getDefaultStringSize());
    }
    
    /**
     * Generate a list of employees with specified count and string size
     */
    public List<Employee> generateEmployees(int count, int stringSize) {
        // Validate input parameters
        count = Math.min(count, properties.getMaxCount());
        count = Math.max(count, 1);
        stringSize = Math.min(stringSize, properties.getMaxStringSize());
        stringSize = Math.max(stringSize, properties.getMinStringSize());
        
        List<Employee> employees = new ArrayList<>(count);
        
        for (int i = 1; i <= count; i++) {
            Employee employee = generateSingleEmployee((long) i, stringSize);
            employees.add(employee);
        }
        
        return employees;
    }
    
    /**
     * Generate a single employee with specified ID and string size
     */
    public Employee generateSingleEmployee(Long id, int stringSize) {
        Employee employee = new Employee();
        
        employee.setId(id);
        employee.setFirstName(FIRST_NAMES[random.nextInt(FIRST_NAMES.length)]);
        employee.setLastName(LAST_NAMES[random.nextInt(LAST_NAMES.length)]);
        employee.setEmail(generateEmail(employee.getFirstName(), employee.getLastName()));
        employee.setPosition(POSITIONS[random.nextInt(POSITIONS.length)]);
        employee.setDepartment(DEPARTMENTS[random.nextInt(DEPARTMENTS.length)]);
        employee.setSalary(generateSalary());
        employee.setHireDate(generateHireDate());
        employee.setConfigurableData(generateConfigurableString(stringSize));
        
        return employee;
    }
    
    /**
     * Generate email address based on first and last name
     */
    private String generateEmail(String firstName, String lastName) {
        return (firstName + "." + lastName + "@company.com").toLowerCase();
    }
    
    /**
     * Generate a random salary between 40,000 and 150,000
     */
    private Double generateSalary() {
        return 40000.0 + (random.nextDouble() * 110000.0);
    }
    
    /**
     * Generate a random hire date within the last 10 years
     */
    private LocalDate generateHireDate() {
        LocalDate now = LocalDate.now();
        int daysBack = random.nextInt(3650); // Up to 10 years back
        return now.minusDays(daysBack);
    }
    
    /**
     * Generate a configurable string of specified size for memory testing
     */
    private String generateConfigurableString(int size) {
        StringBuilder sb = new StringBuilder(size);
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        
        for (int i = 0; i < size; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }
    
    /**
     * Get total memory consumption estimate for a list of employees
     */
    public long calculateTotalMemoryConsumption(List<Employee> employees) {
        return employees.stream()
                .mapToLong(Employee::getApproximateMemorySize)
                .sum();
    }
    
    /**
     * Get statistics about the generated employee data
     */
    public EmployeeDataStats getDataStats(List<Employee> employees) {
        if (employees.isEmpty()) {
            return new EmployeeDataStats(0, 0, 0, 0);
        }
        
        long totalMemory = calculateTotalMemoryConsumption(employees);
        long avgMemoryPerEmployee = totalMemory / employees.size();
        int avgStringSize = (int) employees.stream()
                .mapToInt(emp -> emp.getConfigurableData() != null ? emp.getConfigurableData().length() : 0)
                .average()
                .orElse(0);
        
        return new EmployeeDataStats(employees.size(), totalMemory, avgMemoryPerEmployee, avgStringSize);
    }
    
    /**
     * Statistics class for employee data
     */
    public static class EmployeeDataStats {
        private final int count;
        private final long totalMemoryBytes;
        private final long avgMemoryPerEmployeeBytes;
        private final int avgStringSize;
        
        public EmployeeDataStats(int count, long totalMemoryBytes, long avgMemoryPerEmployeeBytes, int avgStringSize) {
            this.count = count;
            this.totalMemoryBytes = totalMemoryBytes;
            this.avgMemoryPerEmployeeBytes = avgMemoryPerEmployeeBytes;
            this.avgStringSize = avgStringSize;
        }
        
        public int getCount() {
            return count;
        }
        
        public long getTotalMemoryBytes() {
            return totalMemoryBytes;
        }
        
        public long getAvgMemoryPerEmployeeBytes() {
            return avgMemoryPerEmployeeBytes;
        }
        
        public int getAvgStringSize() {
            return avgStringSize;
        }
        
        @Override
        public String toString() {
            return String.format("EmployeeDataStats{count=%d, totalMemory=%d bytes (%.2f MB), avgMemoryPerEmployee=%d bytes, avgStringSize=%d chars}", 
                    count, totalMemoryBytes, totalMemoryBytes / 1024.0 / 1024.0, avgMemoryPerEmployeeBytes, avgStringSize);
        }
    }
}
