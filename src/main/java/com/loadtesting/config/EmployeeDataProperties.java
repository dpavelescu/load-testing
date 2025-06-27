package com.loadtesting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for employee data simulation.
 * These properties allow customization of data generation for different
 * memory consumption scenarios during load testing.
 */
@ConfigurationProperties(prefix = "app.employee")
public class EmployeeDataProperties {
    
    /**
     * Default number of employees to generate
     */
    private int defaultCount = 100;
    
    /**
     * Maximum number of employees that can be generated
     */
    private int maxCount = 10000;
    
    /**
     * Default size of the configurable string field in characters
     */
    private int defaultStringSize = 1024;
    
    /**
     * Maximum size of the configurable string field in characters
     */
    private int maxStringSize = 1048576; // 1MB
    
    /**
     * Minimum size of the configurable string field in characters
     */
    private int minStringSize = 10;
    
    public int getDefaultCount() {
        return defaultCount;
    }
    
    public void setDefaultCount(int defaultCount) {
        this.defaultCount = defaultCount;
    }
    
    public int getMaxCount() {
        return maxCount;
    }
    
    public void setMaxCount(int maxCount) {
        this.maxCount = maxCount;
    }
    
    public int getDefaultStringSize() {
        return defaultStringSize;
    }
    
    public void setDefaultStringSize(int defaultStringSize) {
        this.defaultStringSize = defaultStringSize;
    }
    
    public int getMaxStringSize() {
        return maxStringSize;
    }
    
    public void setMaxStringSize(int maxStringSize) {
        this.maxStringSize = maxStringSize;
    }
    
    public int getMinStringSize() {
        return minStringSize;
    }
    
    public void setMinStringSize(int minStringSize) {
        this.minStringSize = minStringSize;
    }
}
