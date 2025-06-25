package com.loadtesting.controller;

import com.loadtesting.model.Employee;
import com.loadtesting.service.EmployeeDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST controller for employee data endpoints.
 * Provides API endpoints for retrieving employee data with configurable parameters
 * for load testing and resource sizing purposes.
 */
@RestController
@RequestMapping(value = "/api/employees", produces = "application/json")
public class EmployeeController {
    
    private final EmployeeDataService employeeDataService;
    
    public EmployeeController(EmployeeDataService employeeDataService) {
        this.employeeDataService = employeeDataService;
    }
    
    /**
     * Get all employees with default configuration
     * GET /api/employees
     */
    @GetMapping
    public ResponseEntity<List<Employee>> getAllEmployees() {
        List<Employee> employees = employeeDataService.generateEmployees();
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Get employees with specified count
     * GET /api/employees?count=50
     */
    @GetMapping(params = "count")
    public ResponseEntity<List<Employee>> getEmployeesByCount(@RequestParam int count) {
        List<Employee> employees = employeeDataService.generateEmployees(count);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Get employees with specified count and memory size
     * GET /api/employees?count=50&memorySize=2048
     */
    @GetMapping(params = {"count", "memorySize"})
    public ResponseEntity<List<Employee>> getEmployeesWithMemorySize(
            @RequestParam int count, 
            @RequestParam int memorySize) {
        List<Employee> employees = employeeDataService.generateEmployees(count, memorySize);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Get a single employee by ID
     * GET /api/employees/1
     */
    @GetMapping("/{id}")
    public ResponseEntity<Employee> getEmployeeById(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1024") int memorySize) {
        Employee employee = employeeDataService.generateSingleEmployee(id, memorySize);
        return ResponseEntity.ok(employee);
    }
    
    /**
     * Get employees filtered by department
     * GET /api/employees/department/Engineering
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<Employee>> getEmployeesByDepartment(
            @PathVariable String department,
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "1024") int memorySize) {
        List<Employee> employees = employeeDataService.generateEmployees(count, memorySize);
        
        // Filter by department (case-insensitive)
        List<Employee> filteredEmployees = employees.stream()
                .filter(emp -> emp.getDepartment().equalsIgnoreCase(department))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(filteredEmployees);
    }
    
    /**
     * Get employees filtered by position
     * GET /api/employees/position/Manager
     */
    @GetMapping("/position/{position}")
    public ResponseEntity<List<Employee>> getEmployeesByPosition(
            @PathVariable String position,
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "1024") int memorySize) {
        List<Employee> employees = employeeDataService.generateEmployees(count, memorySize);
        
        // Filter by position (case-insensitive)
        List<Employee> filteredEmployees = employees.stream()
                .filter(emp -> emp.getPosition().equalsIgnoreCase(position))
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(filteredEmployees);
    }
    
    /**
     * Health check endpoint for the employee service
     * GET /api/employees/health
     */
    @GetMapping("/health")
    public ResponseEntity<java.util.Map<String, String>> healthCheck() {
        java.util.Map<String, String> response = java.util.Map.of("status", "Employee service is running");
        return ResponseEntity.ok(response);
    }
}
