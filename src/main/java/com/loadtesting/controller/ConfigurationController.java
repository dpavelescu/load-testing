package com.loadtesting.controller;

import com.loadtesting.config.EmployeeDataProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for managing employee data configuration.
 * Provides endpoints to view and modify configuration parameters for load testing.
 */
@RestController
@RequestMapping("/api/config")
public class ConfigurationController {
    
    private final EmployeeDataProperties properties;
    
    public ConfigurationController(EmployeeDataProperties properties) {
        this.properties = properties;
    }
    
    /**
     * Get current configuration settings
     * GET /api/config
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getConfiguration() {
        Map<String, Object> config = new HashMap<>();
        config.put("defaultCount", properties.getDefaultCount());
        config.put("maxCount", properties.getMaxCount());
        config.put("defaultStringSize", properties.getDefaultStringSize());
        config.put("maxStringSize", properties.getMaxStringSize());
        config.put("minStringSize", properties.getMinStringSize());
        
        return ResponseEntity.ok(config);
    }
    
    /**
     * Get memory configuration limits
     * GET /api/config/memory
     */
    @GetMapping("/memory")
    public ResponseEntity<Map<String, Object>> getMemoryConfiguration() {
        Map<String, Object> memoryConfig = new HashMap<>();
        memoryConfig.put("defaultStringSize", properties.getDefaultStringSize());
        memoryConfig.put("maxStringSize", properties.getMaxStringSize());
        memoryConfig.put("minStringSize", properties.getMinStringSize());
        memoryConfig.put("defaultStringSizeBytes", properties.getDefaultStringSize());
        memoryConfig.put("maxStringSizeBytes", properties.getMaxStringSize());
        memoryConfig.put("maxStringSizeMB", properties.getMaxStringSize() / 1024.0 / 1024.0);
        
        return ResponseEntity.ok(memoryConfig);
    }
    
    /**
     * Get count configuration limits
     * GET /api/config/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCountConfiguration() {
        Map<String, Object> countConfig = new HashMap<>();
        countConfig.put("defaultCount", properties.getDefaultCount());
        countConfig.put("maxCount", properties.getMaxCount());
        
        return ResponseEntity.ok(countConfig);
    }
}
