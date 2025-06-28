import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics for monitoring
const errorRate = new Rate('errors');
const responseTimeTrend = new Trend('response_time_trend');
const memoryPressure = new Trend('memory_pressure');
const latencyPressure = new Trend('latency_pressure');

export const options = {
    scenarios: {
        high_pressure_test: {
            executor: 'constant-arrival-rate',
            rate: 6, // Conservative RPS to handle heavy operations
            timeUnit: '1s',
            duration: '6m',
            preAllocatedVUs: 8,   
            maxVUs: 15,
            gracefulStop: '60s', // Increased graceful stop time
        },
    },
    thresholds: {
        'errors': ['rate<0.02'],                    // <2% error rate (higher due to pressure)
        'http_req_failed': ['rate<0.02'],           
        'http_req_duration': ['p(95)<8000'],        // p95 under 8s (accounting for DB delays)
        'response_time_trend': ['p(90)<5000'],      // p90 under 5s
    },
    // Add connection reuse and limits
    discardResponseBodies: false, // Keep response bodies for checks
    noConnectionReuse: false,     // Reuse connections
    batch: 1,                     // Process requests individually for cleaner shutdown
};

export function setup() {
    console.log('=== HIGH PRESSURE STRESS TEST ===');
    console.log('Target RPS: 6 (HEAVY OPERATIONS)');
    console.log('Test Duration: 6m');
    console.log('Focus: Maximum memory and latency pressure');
    console.log('Base URL: http://localhost:8080');
    console.log('');
    console.log('💥 HIGH PRESSURE: Test resource limits with realistic stress');
    console.log('Memory: Large objects, high retention');
    console.log('Latency: Realistic database delays (500-2000ms)');
    console.log('');

    // Health check
    const healthResponse = http.get('http://localhost:8080/actuator/health');
    const healthCheck = check(healthResponse, {
        'Health check passed': (r) => r.status === 200,
        'Service is UP': (r) => r.json() && r.json().status === 'UP',
    });
    
    if (!healthCheck) {
        throw new Error('Service health check failed');
    }

    // Get memory stats
    const memResponse = http.get('http://localhost:8080/api/memory/stats');
    if (memResponse.status === 200) {
        const memStats = memResponse.json();
        const memoryUsage = ((memStats.used / memStats.max) * 100).toFixed(2);
        console.log('=== Pre-Test Resource State ===');
        console.log(`Memory Usage: ${memoryUsage}%`);
        console.log(`Used Memory: ${(memStats.used / (1024 * 1024)).toFixed(2)} MB`);
        console.log(`Max Memory: ${(memStats.max / (1024 * 1024)).toFixed(2)} MB`);
    }
    
    console.log('');
    console.log('Starting HIGH PRESSURE test...');
    return { baseUrl: 'http://localhost:8080' };
}

export default function(data) {
    const baseUrl = data.baseUrl;
    
    // High-pressure request mix with realistic stress
    const requests = [
        // 15% - Health checks (minimal load for baseline)
        () => {
            const response = http.get(`${baseUrl}/actuator/health`);
            memoryPressure.add(1); // Low pressure
            latencyPressure.add(1); // Low pressure
            return { response, operation: 'health_check' };
        },
        
        // 25% - Large employee datasets (HIGH MEMORY PRESSURE)
        () => {
            const count = Math.floor(Math.random() * 800) + 500; // 500-1300 objects (MUCH LARGER)
            const stringSize = Math.floor(Math.random() * 4096) + 2048; // 2KB-6KB per object (LARGE)
            const response = http.get(`${baseUrl}/api/employees?count=${count}&stringSize=${stringSize}`);
            memoryPressure.add(count * stringSize / 1024); // Track KB allocated
            latencyPressure.add(2); // Low latency
            return { response, operation: 'large_employees' };
        },
        
        // 30% - Heavy memory cache operations (EXTREME MEMORY PRESSURE)
        () => {
            const count = Math.floor(Math.random() * 500) + 200; // 200-700 objects (LARGE CACHE)
            const stringSize = Math.floor(Math.random() * 8192) + 4096; // 4KB-12KB per object (HUGE)
            const cacheKey = `pressure-test-${Math.floor(Math.random() * 10)}`; // Limited keys for retention
            const response = http.post(`${baseUrl}/api/memory/cache?cacheKey=${cacheKey}&count=${count}&stringSize=${stringSize}`);
            memoryPressure.add((count * stringSize / 1024) * 2); // Cache retention doubles pressure
            latencyPressure.add(3); // Medium latency
            return { response, operation: 'heavy_memory_cache' };
        },
        
        // 20% - Database latency simulation (HIGH LATENCY PRESSURE)
        () => {
            const delayMs = Math.floor(Math.random() * 1500) + 500; // 500-2000ms (REALISTIC DB DELAYS)
            const count = Math.floor(Math.random() * 100) + 50; // 50-150 objects
            const response = http.get(`${baseUrl}/api/load-test/db-latency?delayMs=${delayMs}&count=${count}`);
            memoryPressure.add(5); // Medium memory
            latencyPressure.add(delayMs); // High latency pressure
            return { response, operation: 'db_latency' };
        },
        
        // 10% - Memory scenario stress (EXTREME SCENARIOS)
        () => {
            const scenarios = ['heavy', 'extreme']; // Only heavy scenarios
            const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
            const response = http.get(`${baseUrl}/api/memory/scenario/${scenario}`);
            memoryPressure.add(scenario === 'extreme' ? 100 : 50); // High pressure scenarios
            latencyPressure.add(4); // Medium latency
            return { response, operation: `scenario_${scenario}` };
        },
    ];
    
    // Weighted selection favoring high-pressure operations
    let requestIndex;
    const rand = Math.random();
    if (rand < 0.15) {
        requestIndex = 0; // Health check (15%)
    } else if (rand < 0.40) {
        requestIndex = 1; // Large employees (25%)
    } else if (rand < 0.70) {
        requestIndex = 2; // Heavy memory cache (30%)
    } else if (rand < 0.90) {
        requestIndex = 3; // DB latency (20%)
    } else {
        requestIndex = 4; // Memory scenarios (10%)
    }
    
    const { response, operation } = requests[requestIndex]();
    
    // Response validation (more lenient due to heavy operations)
    const success = check(response, {
        'Status is 2xx': (r) => r.status >= 200 && r.status < 300,
        'Response time under 8s': (r) => r.timings.duration < 8000, // Higher threshold
        'No connection errors': (r) => r.status !== 0,
        'Response has content': (r) => r.body && r.body.length > 0,
    });
    
    // Record metrics
    errorRate.add(!success);
    responseTimeTrend.add(response.timings.duration);
    
    // Add small sleep for connection cleanup
    sleep(0.1); // Increased sleep for cleaner disconnection
}

export function teardown(data) {
    console.log('');
    console.log('=== HIGH PRESSURE TEST COMPLETED ===');
    console.log('💥 Maximum stress test completed!');
    console.log('');
    
    // Get final memory state
    const memResponse = http.get(`${data.baseUrl}/api/memory/stats`);
    if (memResponse.status === 200) {
        const memStats = memResponse.json();
        const memoryUsage = ((memStats.used / memStats.max) * 100).toFixed(2);
        const memoryMB = (memStats.used / (1024 * 1024)).toFixed(2);
        const maxMemoryMB = (memStats.max / (1024 * 1024)).toFixed(2);
        
        console.log('=== Final Resource Analysis ===');
        console.log(`Memory Usage: ${memoryUsage}%`);
        console.log(`Used Memory: ${memoryMB} MB`);
        console.log(`Max Memory: ${maxMemoryMB} MB`);
        
        let pressure = 'LOW';
        if (parseFloat(memoryUsage) > 80) pressure = 'EXTREME';
        else if (parseFloat(memoryUsage) > 60) pressure = 'HIGH';
        else if (parseFloat(memoryUsage) > 40) pressure = 'MEDIUM';
        
        console.log(`Memory Pressure: ${pressure}`);
        console.log('');
        
        console.log('=== HIGH PRESSURE IMPACT ===');
        console.log('Health Checks: Baseline monitoring (15% load)');
        console.log('Large Employees: 500-1300 objects, 2-6KB each (25% load)');
        console.log('Heavy Memory Cache: 200-700 objects, 4-12KB each, retained (30% load)');
        console.log('Database Latency: 500-2000ms realistic delays (20% load)');
        console.log('Memory Scenarios: Heavy/Extreme predefined scenarios (10% load)');
        console.log('Combined RPS: 6 requests/second sustained');
        console.log('✅ Maximum realistic stress applied');
        console.log('');
        
        console.log('=== RESOURCE VALIDATION ===');
        console.log('Current Allocation: 360Mi memory, 25m CPU');
        console.log('Stress Level: MAXIMUM - Large objects, cache retention, DB delays');
        console.log('Expected: Higher memory usage, potential pressure points revealed');
        console.log('');
        
        console.log('=== PRESSURE ANALYSIS ===');
        console.log('Memory Pressure: Up to 12KB per object × 700 objects = 8.4MB per request');
        console.log('Cache Retention: Multiple cache keys retained simultaneously');
        console.log('Latency Pressure: Up to 2 second delays per DB request');
        console.log('Combined Load: Realistic production-level stress patterns');
    }
}
