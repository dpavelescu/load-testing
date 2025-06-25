package com.loadtesting.service;

import com.loadtesting.config.MemorySimulationProperties;
import com.loadtesting.model.Employee;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for simulating memory consumption through configurable data generation
 */
@Service
public class MemorySimulationService {
    
    private final EmployeeDataService employeeDataService;
    private final MemorySimulationProperties memoryProperties;
    private final ConcurrentHashMap<String, List<Employee>> memoryCache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    
    public MemorySimulationService(EmployeeDataService employeeDataService, 
                                 MemorySimulationProperties memoryProperties) {
        this.employeeDataService = employeeDataService;
        this.memoryProperties = memoryProperties;
        
        // Start memory stress testing if enabled
        if (memoryProperties.getStress().isEnabled()) {
            startMemoryStressTesting();
        }
    }
    
    /**
     * Generate employee data using predefined memory scenarios
     */
    public List<Employee> generateByScenario(String scenarioName) {
        if (!memoryProperties.getSimulation().isEnabled()) {
            throw new IllegalStateException("Memory simulation is not enabled");
        }
        
        var scenario = memoryProperties.getSimulation().getScenarios().get(scenarioName);
        if (scenario == null) {
            throw new IllegalArgumentException("Unknown scenario: " + scenarioName);
        }
        
        return employeeDataService.generateEmployees(scenario.getCount(), scenario.getStringSize());
    }
    
    /**
     * Generate and cache employee data for memory retention testing
     */
    public String generateAndCache(String cacheKey, int count, int stringSize) {
        List<Employee> employees = employeeDataService.generateEmployees(count, stringSize);
        memoryCache.put(cacheKey, employees);
        
        // Schedule cache cleanup
        scheduler.schedule(() -> {
            memoryCache.remove(cacheKey);
        }, memoryProperties.getStress().getRetentionTimeSeconds(), TimeUnit.SECONDS);
        
        return String.format("Generated and cached %d employees with string size %d bytes. " +
                           "Cache key: %s. Will be cleaned up in %d seconds.", 
                           count, stringSize, cacheKey, memoryProperties.getStress().getRetentionTimeSeconds());
    }
    
    /**
     * Get cached data information
     */
    public MemoryCacheInfo getCacheInfo() {
        int totalEntries = memoryCache.size();
        int totalEmployees = memoryCache.values().stream()
                .mapToInt(List::size)
                .sum();
        
        long estimatedMemoryBytes = memoryCache.values().stream()
                .flatMap(List::stream)
                .mapToLong(this::estimateEmployeeMemorySize)
                .sum();
        
        return new MemoryCacheInfo(totalEntries, totalEmployees, estimatedMemoryBytes);
    }
    
    /**
     * Clear all cached data
     */
    public String clearCache() {
        int clearedEntries = memoryCache.size();
        memoryCache.clear();
        return String.format("Cleared %d cache entries", clearedEntries);
    }
    
    /**
     * Get available memory scenarios
     */
    public List<String> getAvailableScenarios() {
        return new ArrayList<>(memoryProperties.getSimulation().getScenarios().keySet());
    }
    
    /**
     * Force garbage collection (for testing purposes)
     */
    public String forceGarbageCollection() {
        long beforeMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        System.gc();
        Thread.yield(); // Give GC a chance to run
        long afterMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        
        return String.format("Garbage collection requested. Memory before: %d bytes, after: %d bytes, freed: %d bytes",
                           beforeMemory, afterMemory, beforeMemory - afterMemory);
    }
    
    private void startMemoryStressTesting() {
        scheduler.scheduleAtFixedRate(this::performMemoryStressTest, 
                                    memoryProperties.getStress().getGcFrequencySeconds(),
                                    memoryProperties.getStress().getGcFrequencySeconds(),
                                    TimeUnit.SECONDS);
    }
    
    private void performMemoryStressTest() {
        // Generate temporary data to stress memory
        String tempKey = "stress_test_" + System.currentTimeMillis();
        generateAndCache(tempKey, 100, 4096);
    }
    
    private long estimateEmployeeMemorySize(Employee employee) {
        // Rough estimation of memory usage per employee
        return 200 + // Base object overhead
               (employee.getFirstName() != null ? employee.getFirstName().length() * 2 : 0) +
               (employee.getLastName() != null ? employee.getLastName().length() * 2 : 0) +
               (employee.getEmail() != null ? employee.getEmail().length() * 2 : 0) +
               (employee.getDepartment() != null ? employee.getDepartment().length() * 2 : 0) +
               (employee.getPosition() != null ? employee.getPosition().length() * 2 : 0);
    }
    
    /**
     * Inner class for cache information
     */
    public static class MemoryCacheInfo {
        private final int cacheEntries;
        private final int totalEmployees;
        private final long estimatedMemoryBytes;
        
        public MemoryCacheInfo(int cacheEntries, int totalEmployees, long estimatedMemoryBytes) {
            this.cacheEntries = cacheEntries;
            this.totalEmployees = totalEmployees;
            this.estimatedMemoryBytes = estimatedMemoryBytes;
        }
        
        public int getCacheEntries() { return cacheEntries; }
        public int getTotalEmployees() { return totalEmployees; }
        public long getEstimatedMemoryBytes() { return estimatedMemoryBytes; }
        public String getEstimatedMemoryMB() { 
            return String.format("%.2f MB", estimatedMemoryBytes / (1024.0 * 1024.0)); 
        }
    }
}
