package com.loadtesting.controller;

import com.loadtesting.model.Employee;
import com.loadtesting.service.EmployeeDataService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller specifically designed for load testing scenarios.
 * Provides endpoints with different memory consumption patterns and response sizes.
 */
@RestController
@RequestMapping("/api/load-test")
public class LoadTestController {
    
    private final EmployeeDataService employeeDataService;
    
    public LoadTestController(EmployeeDataService employeeDataService) {
        this.employeeDataService = employeeDataService;
    }
    
    /**
     * Light load endpoint - small response
     * GET /api/load-test/light?count=10
     */
    @GetMapping("/light")
    public ResponseEntity<List<Employee>> lightLoad(@RequestParam(defaultValue = "10") int count) {
        // Small memory footprint - 100 bytes per employee
        List<Employee> employees = employeeDataService.generateEmployees(
            Math.min(count, 50), 100);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Medium load endpoint - medium response
     * GET /api/load-test/medium?count=50
     */
    @GetMapping("/medium")
    public ResponseEntity<List<Employee>> mediumLoad(@RequestParam(defaultValue = "50") int count) {
        // Medium memory footprint - 1KB per employee
        List<Employee> employees = employeeDataService.generateEmployees(
            Math.min(count, 200), 1024);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Heavy load endpoint - large response
     * GET /api/load-test/heavy?count=100
     */
    @GetMapping("/heavy")
    public ResponseEntity<List<Employee>> heavyLoad(@RequestParam(defaultValue = "100") int count) {
        // Large memory footprint - 10KB per employee
        List<Employee> employees = employeeDataService.generateEmployees(
            Math.min(count, 500), 10240);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Memory stress endpoint - very large response
     * GET /api/load-test/memory-stress?count=50&memoryPerEmployee=50000
     */
    @GetMapping("/memory-stress")
    public ResponseEntity<List<Employee>> memoryStress(
            @RequestParam(defaultValue = "50") int count,
            @RequestParam(defaultValue = "50000") int memoryPerEmployee) {
        // Configurable memory stress test
        List<Employee> employees = employeeDataService.generateEmployees(
            Math.min(count, 1000), 
            Math.min(memoryPerEmployee, 1048576)); // Max 1MB per employee
        return ResponseEntity.ok(employees);
    }
    
    /**
     * CPU intensive endpoint - simulates processing delay
     * GET /api/load-test/cpu-intensive?iterations=1000
     */
    @GetMapping("/cpu-intensive")
    public ResponseEntity<Map<String, Object>> cpuIntensive(
            @RequestParam(defaultValue = "1000") int iterations) {
        
        long startTime = System.currentTimeMillis();
        
        // Simulate CPU-intensive work
        double result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.sqrt(i) * Math.sin(i) * Math.cos(i);
        }
        
        long endTime = System.currentTimeMillis();
        long processingTime = endTime - startTime;
        
        Map<String, Object> response = new HashMap<>();
        response.put("iterations", iterations);
        response.put("processingTimeMs", processingTime);
        response.put("result", result);
        response.put("timestamp", startTime);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Database latency simulation endpoint
     * GET /api/load-test/db-latency?delayMs=100
     */
    @GetMapping("/db-latency")
    public ResponseEntity<List<Employee>> databaseLatencySimulation(
            @RequestParam(defaultValue = "100") int delayMs,
            @RequestParam(defaultValue = "20") int count) {
        
        try {
            // Simulate database latency
            Thread.sleep(Math.min(delayMs, 5000)); // Max 5 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        List<Employee> employees = employeeDataService.generateEmployees(
            Math.min(count, 100), 1024);
        return ResponseEntity.ok(employees);
    }
    
    /**
     * Simple ping endpoint for basic connectivity testing
     * GET /api/load-test/ping
     */
    @GetMapping("/ping")
    public ResponseEntity<Map<String, Object>> ping() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("timestamp", System.currentTimeMillis());
        response.put("service", "resource-sizing-service");
        
        return ResponseEntity.ok(response);
    }
}
