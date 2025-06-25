package com.loadtesting.controller;

import com.loadtesting.model.Employee;
import com.loadtesting.service.MemorySimulationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for memory simulation and testing endpoints
 */
@RestController
@RequestMapping("/api/memory")
public class MemorySimulationController {
    
    private final MemorySimulationService memorySimulationService;
    
    public MemorySimulationController(MemorySimulationService memorySimulationService) {
        this.memorySimulationService = memorySimulationService;
    }
    
    /**
     * Generate employee data using predefined memory scenarios
     * Available scenarios: light, medium, heavy, extreme
     */
    @GetMapping("/scenario/{scenarioName}")
    public ResponseEntity<List<Employee>> generateByScenario(@PathVariable String scenarioName) {
        try {
            List<Employee> employees = memorySimulationService.generateByScenario(scenarioName);
            return ResponseEntity.ok(employees);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    /**
     * Generate and cache employee data for memory retention testing
     */
    @PostMapping("/cache")
    public ResponseEntity<Map<String, String>> generateAndCache(
            @RequestParam String cacheKey,
            @RequestParam(defaultValue = "100") int count,
            @RequestParam(defaultValue = "1024") int stringSize) {
        
        String result = memorySimulationService.generateAndCache(cacheKey, count, stringSize);
        return ResponseEntity.ok(Map.of("message", result));
    }
    
    /**
     * Get current memory cache information
     */
    @GetMapping("/cache/info")
    public ResponseEntity<MemorySimulationService.MemoryCacheInfo> getCacheInfo() {
        return ResponseEntity.ok(memorySimulationService.getCacheInfo());
    }
    
    /**
     * Clear all cached data
     */
    @DeleteMapping("/cache")
    public ResponseEntity<Map<String, String>> clearCache() {
        String result = memorySimulationService.clearCache();
        return ResponseEntity.ok(Map.of("message", result));
    }
    
    /**
     * Get available memory scenarios
     */
    @GetMapping("/scenarios")
    public ResponseEntity<List<String>> getAvailableScenarios() {
        return ResponseEntity.ok(memorySimulationService.getAvailableScenarios());
    }
    
    /**
     * Force garbage collection (for testing purposes)
     */
    @PostMapping("/gc")
    public ResponseEntity<Map<String, String>> forceGarbageCollection() {
        String result = memorySimulationService.forceGarbageCollection();
        return ResponseEntity.ok(Map.of("message", result));
    }
    
    /**
     * Get current JVM memory statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getMemoryStats() {
        Runtime runtime = Runtime.getRuntime();
        
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        Map<String, Object> stats = Map.of(
            "maxMemoryBytes", maxMemory,
            "totalMemoryBytes", totalMemory,
            "freeMemoryBytes", freeMemory,
            "usedMemoryBytes", usedMemory,
            "maxMemoryMB", String.format("%.2f MB", maxMemory / (1024.0 * 1024.0)),
            "totalMemoryMB", String.format("%.2f MB", totalMemory / (1024.0 * 1024.0)),
            "freeMemoryMB", String.format("%.2f MB", freeMemory / (1024.0 * 1024.0)),
            "usedMemoryMB", String.format("%.2f MB", usedMemory / (1024.0 * 1024.0)),
            "memoryUsagePercentage", String.format("%.2f%%", (usedMemory * 100.0) / totalMemory)
        );
        
        return ResponseEntity.ok(stats);
    }
}
