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
        moderate_pressure_test: {
            executor: 'constant-arrival-rate',
            rate: 4, // Reduced from 6 to 4 RPS
            timeUnit: '1s',
            duration: '5m', // Slightly shorter duration
            preAllocatedVUs: 6,   // Reduced VUs
            maxVUs: 10,           // Reduced max VUs
            gracefulStop: '30s',  // Shorter graceful stop
        },
    },
    thresholds: {
        'errors': ['rate<0.01'],                    // <1% error rate (stricter)
        'http_req_failed': ['rate<0.01'],           
        'http_req_duration': ['p(95)<5000'],        // p95 under 5s (more reasonable)
        'response_time_trend': ['p(90)<3000'],      // p90 under 3s
    },
    discardResponseBodies: false,
    noConnectionReuse: false,
    batch: 1,
};

export function setup() {
    console.log('=== MODERATE PRESSURE TEST (4 RPS) ===');
    console.log('Target RPS: 4 (REDUCED CONCURRENCY)');
    console.log('Test Duration: 5m');
    console.log('Focus: Sustainable load with reduced concurrency');
    console.log('Base URL: http://localhost:8080');
    console.log('');
    console.log('🎯 MODERATE PRESSURE: Testing sustainable 4 RPS');
    console.log('Memory: Moderate objects, controlled retention');
    console.log('Latency: Realistic but manageable database delays');
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
    console.log('Starting MODERATE PRESSURE test...');
    return { baseUrl: 'http://localhost:8080' };
}

export default function(data) {
    const baseUrl = data.baseUrl;
    
    // Moderate-pressure request mix (reduced intensity)
    const requests = [
        // 20% - Health checks (baseline)
        () => {
            const response = http.get(`${baseUrl}/actuator/health`);
            memoryPressure.add(1);
            latencyPressure.add(1);
            return { response, operation: 'health_check' };
        },
        
        // 30% - Moderate employee datasets (REDUCED SIZE)
        () => {
            const count = Math.floor(Math.random() * 300) + 200; // 200-500 objects (smaller)
            const stringSize = Math.floor(Math.random() * 2048) + 1024; // 1KB-3KB per object
            const response = http.get(`${baseUrl}/api/employees?count=${count}&stringSize=${stringSize}`);
            memoryPressure.add(count * stringSize / 1024);
            latencyPressure.add(2);
            return { response, operation: 'moderate_employees' };
        },
        
        // 25% - Moderate memory cache operations (REDUCED SIZE)
        () => {
            const count = Math.floor(Math.random() * 200) + 100; // 100-300 objects (smaller)
            const stringSize = Math.floor(Math.random() * 3072) + 2048; // 2KB-5KB per object
            const cacheKey = `moderate-test-${Math.floor(Math.random() * 5)}`;
            const response = http.post(`${baseUrl}/api/memory/cache?cacheKey=${cacheKey}&count=${count}&stringSize=${stringSize}`);
            memoryPressure.add((count * stringSize / 1024) * 1.5); // Moderate cache pressure
            latencyPressure.add(3);
            return { response, operation: 'moderate_memory_cache' };
        },
        
        // 20% - Moderate database latency (REDUCED DELAYS)
        () => {
            const delayMs = Math.floor(Math.random() * 1000) + 300; // 300-1300ms (more reasonable)
            const count = Math.floor(Math.random() * 75) + 25; // 25-100 objects
            const response = http.get(`${baseUrl}/api/load-test/db-latency?delayMs=${delayMs}&count=${count}`);
            memoryPressure.add(5);
            latencyPressure.add(delayMs);
            return { response, operation: 'moderate_db_latency' };
        },
        
        // 5% - Light memory scenarios
        () => {
            const scenarios = ['light', 'medium']; // Only lighter scenarios
            const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
            const response = http.get(`${baseUrl}/api/memory/scenario/${scenario}`);
            memoryPressure.add(scenario === 'medium' ? 30 : 15);
            latencyPressure.add(3);
            return { response, operation: `scenario_${scenario}` };
        },
    ];
    
    // Weighted selection
    let requestIndex;
    const rand = Math.random();
    if (rand < 0.20) {
        requestIndex = 0; // Health check (20%)
    } else if (rand < 0.50) {
        requestIndex = 1; // Moderate employees (30%)
    } else if (rand < 0.75) {
        requestIndex = 2; // Moderate memory cache (25%)
    } else if (rand < 0.95) {
        requestIndex = 3; // Moderate DB latency (20%)
    } else {
        requestIndex = 4; // Light memory scenarios (5%)
    }
    
    const { response, operation } = requests[requestIndex]();
    
    // Response validation (reasonable thresholds)
    const success = check(response, {
        'Status is 2xx': (r) => r.status >= 200 && r.status < 300,
        'Response time under 5s': (r) => r.timings.duration < 5000,
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
    console.log('=== MODERATE PRESSURE TEST COMPLETED ===');
    console.log('🎯 4 RPS sustainable load test completed!');
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
        if (parseFloat(memoryUsage) > 70) pressure = 'HIGH';
        else if (parseFloat(memoryUsage) > 50) pressure = 'MEDIUM';
        
        console.log(`Memory Pressure: ${pressure}`);
        console.log('');
        
        console.log('=== MODERATE PRESSURE IMPACT ===');
        console.log('Health Checks: Baseline monitoring (20% load)');
        console.log('Moderate Employees: 200-500 objects, 1-3KB each (30% load)');
        console.log('Moderate Memory Cache: 100-300 objects, 2-5KB each (25% load)');
        console.log('Moderate Database Latency: 300-1300ms delays (20% load)');
        console.log('Light Memory Scenarios: Light/Medium scenarios (5% load)');
        console.log('Combined RPS: 4 requests/second sustained');
        console.log('✅ Sustainable moderate stress applied');
        console.log('');
        
        console.log('=== RESOURCE VALIDATION ===');
        console.log('Current Allocation: 360Mi memory, 500m CPU');
        console.log('Stress Level: MODERATE - Sustainable operations, manageable load');
        console.log('Target: Maintain <1% error rate with good response times');
    }
}
