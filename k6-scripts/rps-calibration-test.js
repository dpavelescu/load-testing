import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Trend } from 'k6/metrics';

// Custom metrics for RPS calibration
const errorRate = new Rate('errors');
const responseTimeTrend = new Trend('response_time_trend');

export const options = {
    scenarios: {
        // Scenario 1: Low RPS baseline
        low_rps: {
            executor: 'constant-arrival-rate',
            rate: 2, // 2 RPS
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 3,
            maxVUs: 5,
            startTime: '0s',
            tags: { scenario: 'low_rps' },
        },
        
        // Scenario 2: Medium RPS 
        medium_rps: {
            executor: 'constant-arrival-rate',
            rate: 6, // 6 RPS (your current)
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 8,
            maxVUs: 12,
            startTime: '2m30s',
            tags: { scenario: 'medium_rps' },
        },
        
        // Scenario 3: High RPS to find breaking point
        high_rps: {
            executor: 'constant-arrival-rate',
            rate: 12, // 12 RPS
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 15,
            maxVUs: 20,
            startTime: '5m',
            tags: { scenario: 'high_rps' },
        },
        
        // Scenario 4: Maximum RPS
        max_rps: {
            executor: 'constant-arrival-rate',
            rate: 20, // 20 RPS
            timeUnit: '1s',
            duration: '2m',
            preAllocatedVUs: 25,
            maxVUs: 35,
            startTime: '7m30s',
            tags: { scenario: 'max_rps' },
        },
    },
    thresholds: {
        'errors': ['rate<0.01'],
        'http_req_failed': ['rate<0.01'],
        'http_req_duration': ['p(95)<5000'],
        'response_time_trend': ['p(90)<3000'],
    },
};

export function setup() {
    console.log('=== RPS CALIBRATION TEST ===');
    console.log('Testing different RPS levels to find optimal resource usage');
    console.log('2 RPS → 6 RPS → 12 RPS → 20 RPS');
    console.log('Monitor: kubectl top pods -n load-testing --watch');
    console.log('');
    
    const healthResponse = http.get('http://localhost:8080/actuator/health');
    check(healthResponse, {
        'Health check passed': (r) => r.status === 200,
    });
    
    return { baseUrl: 'http://localhost:8080' };
}

export default function(data) {
    const baseUrl = data.baseUrl;
    
    // Mix of operations similar to your high-pressure test
    const operations = [
        // 40% - Memory-intensive operations
        () => {
            const count = Math.floor(Math.random() * 300) + 100;
            const stringSize = Math.floor(Math.random() * 2048) + 1024;
            return http.get(`${baseUrl}/api/employees?count=${count}&stringSize=${stringSize}`);
        },
        
        // 30% - Cache operations
        () => {
            const count = Math.floor(Math.random() * 200) + 50;
            const stringSize = Math.floor(Math.random() * 4096) + 2048;
            const cacheKey = `rps-test-${Math.floor(Math.random() * 5)}`;
            return http.post(`${baseUrl}/api/memory/cache?cacheKey=${cacheKey}&count=${count}&stringSize=${stringSize}`);
        },
        
        // 20% - DB latency
        () => {
            const delayMs = Math.floor(Math.random() * 1000) + 300;
            const count = Math.floor(Math.random() * 50) + 25;
            return http.get(`${baseUrl}/api/load-test/db-latency?delayMs=${delayMs}&count=${count}`);
        },
        
        // 10% - Health checks
        () => {
            return http.get(`${baseUrl}/actuator/health`);
        },
    ];
    
    // Select operation based on weights
    const rand = Math.random();
    let operation;
    if (rand < 0.4) operation = operations[0];
    else if (rand < 0.7) operation = operations[1];
    else if (rand < 0.9) operation = operations[2];
    else operation = operations[3];
    
    const response = operation();
    
    const success = check(response, {
        'Status is 2xx': (r) => r.status >= 200 && r.status < 300,
        'Response time acceptable': (r) => r.timings.duration < 5000,
    });
    
    errorRate.add(!success);
    responseTimeTrend.add(response.timings.duration);
}

export function teardown(data) {
    console.log('');
    console.log('=== RPS CALIBRATION COMPLETE ===');
    console.log('Check resource usage patterns at different RPS levels');
    console.log('Optimal RPS = highest sustainable rate with <1% errors');
    console.log('');
}
