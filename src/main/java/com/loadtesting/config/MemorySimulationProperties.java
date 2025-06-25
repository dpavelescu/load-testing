package com.loadtesting.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.HashMap;

/**
 * Configuration properties for memory simulation scenarios
 */
@Configuration
@ConfigurationProperties(prefix = "app.employee.memory")
public class MemorySimulationProperties {
    
    private Simulation simulation = new Simulation();
    private Stress stress = new Stress();
    
    public Simulation getSimulation() {
        return simulation;
    }
    
    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }
    
    public Stress getStress() {
        return stress;
    }
    
    public void setStress(Stress stress) {
        this.stress = stress;
    }
    
    public static class Simulation {
        private boolean enabled = true;
        private Map<String, Scenario> scenarios = new HashMap<>();
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public Map<String, Scenario> getScenarios() {
            return scenarios;
        }
        
        public void setScenarios(Map<String, Scenario> scenarios) {
            this.scenarios = scenarios;
        }
    }
    
    public static class Scenario {
        private int count;
        private int stringSize;
        
        public int getCount() {
            return count;
        }
        
        public void setCount(int count) {
            this.count = count;
        }
        
        public int getStringSize() {
            return stringSize;
        }
        
        public void setStringSize(int stringSize) {
            this.stringSize = stringSize;
        }
    }
    
    public static class Stress {
        private boolean enabled = false;
        private int retentionTimeSeconds = 60;
        private int gcFrequencySeconds = 30;
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public int getRetentionTimeSeconds() {
            return retentionTimeSeconds;
        }
        
        public void setRetentionTimeSeconds(int retentionTimeSeconds) {
            this.retentionTimeSeconds = retentionTimeSeconds;
        }
        
        public int getGcFrequencySeconds() {
            return gcFrequencySeconds;
        }
        
        public void setGcFrequencySeconds(int gcFrequencySeconds) {
            this.gcFrequencySeconds = gcFrequencySeconds;
        }
    }
}
