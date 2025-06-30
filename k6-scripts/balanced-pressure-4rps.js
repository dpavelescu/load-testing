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
        balanced_pressure_test: {
            executor: 'constant-arrival-rate',
            rate: 4, // 4 RPS with meaningful pressure
            timeUnit: '1s',
            duration: '5m',
            preAllocatedVUs: 8,   // Increased for better handling
            maxVUs: 12,           // Increased for memory-intensive operations
            gracefulStop: '45s',
        },
    },
    thresholds: {
        'errors': ['rate<0.01'],                    // <1% error rate
        'http_req_failed': ['rate<0.01'],           
        'http_req_duration': ['p(95)<6000'],        // p95 under 6s (realistic for memory ops)
        'response_time_trend': ['p(90)<4000'],      // p90 under 4s
    },
    discardResponseBodies: false,
    noConnectionReuse: false,
    batch: 1,
};

export function setup() {
    console.log('=== BALANCED PRESSURE TEST (4 RPS) ===');
    console.log('Target RPS: 4 (MEANINGFUL PRESSURE)');
    console.log('Test Duration: 5m');
    console.log('Focus: Realistic memory + latency pressure at sustainable RPS');
    console.log('Base URL: http://localhost:30080 (NodePort - True Load Balancing)');
    console.log('');
    console.log('⚖️ BALANCED PRESSURE: Testing meaningful 4 RPS with realistic stress');
    console.log('Memory: Significant objects, realistic retention patterns');
    console.log('Latency: Production-like database delays');
    console.log('🔄 LOAD BALANCING: Using NodePort for true Kubernetes service distribution');
    console.log('');

    // Health check
    const healthResponse = http.get('http://localhost:30080/actuator/health');
    const healthCheck = check(healthResponse, {
        'Health check passed': (r) => r.status === 200,
        'Service is UP': (r) => r.json() && r.json().status === 'UP',
    });
    
    if (!healthCheck) {
        throw new Error('Service health check failed');
    }

    // Get memory stats
    const memResponse = http.get('http://localhost:30080/api/memory/stats');
    if (memResponse.status === 200) {
        const memStats = memResponse.json();
        const memoryUsage = ((memStats.used / memStats.max) * 100).toFixed(2);
        console.log('=== Pre-Test Resource State ===');
        console.log(`Memory Usage: ${memoryUsage}%`);
        console.log(`Used Memory: ${(memStats.used / (1024 * 1024)).toFixed(2)} MB`);
        console.log(`Max Memory: ${(memStats.max / (1024 * 1024)).toFixed(2)} MB`);
    }
    
    console.log('');
    console.log('Starting BALANCED PRESSURE test...');
    return { baseUrl: 'http://localhost:30080' };
}

export default function(data) {
    const baseUrl = data.baseUrl;
    
    // Balanced-pressure request mix (meaningful but sustainable)
    const requests = [
        // 15% - Health checks (minimal load)
        () => {
            const response = http.get(`${baseUrl}/actuator/health`);
            memoryPressure.add(1);
            latencyPressure.add(1);
            return { response, operation: 'health_check' };
        },
        
        // 35% - Significant employee datasets (MEANINGFUL MEMORY PRESSURE)
        () => {
            const count = Math.floor(Math.random() * 600) + 300; // 300-900 objects (significant)
            const stringSize = Math.floor(Math.random() * 3072) + 2048; // 2KB-5KB per object (meaningful)
            const response = http.get(`${baseUrl}/api/employees?count=${count}&stringSize=${stringSize}`);
            memoryPressure.add(count * stringSize / 1024); // Track KB allocated
            latencyPressure.add(2);
            return { response, operation: 'significant_employees' };
        },
        
        // 25% - Heavy memory cache operations (HIGH MEMORY PRESSURE)
        () => {
            const count = Math.floor(Math.random() * 400) + 150; // 150-550 objects (heavy)
            const stringSize = Math.floor(Math.random() * 6144) + 3072; // 3KB-9KB per object (heavy)
            const cacheKey = `balanced-test-${Math.floor(Math.random() * 8)}`; // Limited keys for retention
            const response = http.post(`${baseUrl}/api/memory/cache?cacheKey=${cacheKey}&count=${count}&stringSize=${stringSize}`);
            memoryPressure.add((count * stringSize / 1024) * 1.8); // High cache pressure
            latencyPressure.add(3);
            return { response, operation: 'heavy_memory_cache' };
        },
        
        // 20% - Realistic database latency (HIGH LATENCY PRESSURE)
        () => {
            const delayMs = Math.floor(Math.random() * 1200) + 400; // 400-1600ms (realistic production delays)
            const count = Math.floor(Math.random() * 80) + 40; // 40-120 objects
            const response = http.get(`${baseUrl}/api/load-test/db-latency?delayMs=${delayMs}&count=${count}`);
            memoryPressure.add(8); // Moderate memory impact
            latencyPressure.add(delayMs);
            return { response, operation: 'realistic_db_latency' };
        },
        
        // 5% - Memory scenario stress (BALANCED SCENARIOS)
        () => {
            const scenarios = ['medium', 'heavy']; // Meaningful scenarios
            const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
            const response = http.get(`${baseUrl}/api/memory/scenario/${scenario}`);
            memoryPressure.add(scenario === 'heavy' ? 60 : 35); // Balanced pressure
            latencyPressure.add(4);
            return { response, operation: `scenario_${scenario}` };
        },
    ];
    
    // Weighted selection
    let requestIndex;
    const rand = Math.random();
    if (rand < 0.15) {
        requestIndex = 0; // Health check (15%)
    } else if (rand < 0.50) {
        requestIndex = 1; // Significant employees (35%)
    } else if (rand < 0.75) {
        requestIndex = 2; // Heavy memory cache (25%)
    } else if (rand < 0.95) {
        requestIndex = 3; // Realistic DB latency (20%)
    } else {
        requestIndex = 4; // Memory scenarios (5%)
    }
    
    const { response, operation } = requests[requestIndex]();
    
    // Response validation (balanced thresholds)
    const success = check(response, {
        'Status is 2xx': (r) => r.status >= 200 && r.status < 300,
        'Response time under 6s': (r) => r.timings.duration < 6000, // Realistic for memory ops
        'No connection errors': (r) => r.status !== 0,
        'Response has content': (r) => r.body && r.body.length > 0,
    });
    
    // Record metrics
    errorRate.add(!success);
    responseTimeTrend.add(response.timings.duration);
    
    // Small sleep for connection management
    sleep(0.05);
}

export function teardown(data) {
    console.log('');
    console.log('=== BALANCED PRESSURE TEST COMPLETED ===');
    console.log('⚖️ 4 RPS meaningful pressure test completed!');
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
        if (parseFloat(memoryUsage) > 75) pressure = 'HIGH';
        else if (parseFloat(memoryUsage) > 50) pressure = 'MEDIUM';
        
        console.log(`Memory Pressure: ${pressure}`);
        console.log('');
        
        console.log('=== BALANCED PRESSURE IMPACT ===');
        console.log('Health Checks: Baseline monitoring (15% load)');
        console.log('Significant Employees: 300-900 objects, 2-5KB each (35% load)');
        console.log('Heavy Memory Cache: 150-550 objects, 3-9KB each, retained (25% load)');
        console.log('Realistic Database Latency: 400-1600ms production delays (20% load)');
        console.log('Memory Scenarios: Medium/Heavy predefined scenarios (5% load)');
        console.log('Combined RPS: 4 requests/second sustained');
        console.log('✅ Meaningful realistic stress applied');
        console.log('');
        
        console.log('=== MEMORY PRESSURE ANALYSIS ===');
        console.log('Employee Memory: Up to 5KB × 900 objects = 4.5MB per request');
        console.log('Cache Memory: Up to 9KB × 550 objects = 5MB per request (retained)');
        console.log('Combined Peak: ~10MB per request cycle with cache retention');
        console.log('Latency Impact: Up to 1.6s delays creating backpressure');
        console.log('Expected Resource Usage: 200-400Mi memory, 300-600m CPU');
        console.log('');
        
        console.log('=== CALIBRATION TARGETS ===');
        console.log('Success Rate: >99% (realistic production target)');
        console.log('Response Time: p95 < 6s, p90 < 4s (accounting for DB delays)');
        console.log('Memory Utilization: 50-75% of allocated resources');
        console.log('CPU Utilization: 60-80% of allocated resources');
    }
}
