import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');

// Test configuration
export const options = {
  stages: [
    { duration: '30s', target: 10 },   // Ramp up to 10 users over 30 seconds
    { duration: '1m', target: 10 },    // Stay at 10 users for 1 minute
    { duration: '30s', target: 20 },   // Ramp up to 20 users over 30 seconds
    { duration: '2m', target: 20 },    // Stay at 20 users for 2 minutes
    { duration: '30s', target: 0 },    // Ramp down to 0 users over 30 seconds
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'],  // 95% of requests should be below 500ms
    http_req_failed: ['rate<0.1'],     // Error rate should be less than 10%
    errors: ['rate<0.1'],              // Custom error rate should be less than 10%
  },
};

// Base URL - can be overridden with environment variable
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  // Check if the service is healthy before starting the test
  const healthCheck = http.get(`${BASE_URL}/actuator/health`);
  check(healthCheck, {
    'Health check status is 200': (r) => r.status === 200,
    'Service is UP': (r) => r.json('status') === 'UP',
  });
  
  console.log(`Starting load test against: ${BASE_URL}`);
  console.log(`Health check status: ${healthCheck.json('status')}`);
}

export default function () {
  // Test scenarios with different employee counts and string sizes
  const scenarios = [
    { endpoint: '/api/employees', params: { count: 50, stringSize: 256 } },
    { endpoint: '/api/employees', params: { count: 100, stringSize: 512 } },
    { endpoint: '/api/employees', params: { count: 200, stringSize: 1024 } },
  ];
  
  // Randomly select a scenario
  const scenario = scenarios[Math.floor(Math.random() * scenarios.length)];
  
  // Build URL with query parameters
  const url = `${BASE_URL}${scenario.endpoint}?count=${scenario.params.count}&stringSize=${scenario.params.stringSize}`;
  
  // Make the request
  const response = http.get(url, {
    headers: {
      'Accept': 'application/json',
      'User-Agent': 'k6-load-test/1.0',
    },
    timeout: '30s',
  });
  
  // Validate response
  const result = check(response, {
    'Status is 200': (r) => r.status === 200,
    'Response time < 500ms': (r) => r.timings.duration < 500,
    'Response time < 1000ms': (r) => r.timings.duration < 1000,
    'Response contains employees': (r) => {
      try {
        const body = r.json();
        return Array.isArray(body) && body.length > 0;
      } catch (e) {
        return false;
      }
    },
    'Response size is reasonable': (r) => r.body.length > 100, // At least some data
  });
  
  // Record errors
  errorRate.add(!result);
  
  // Log scenario details occasionally
  if (__ITER % 10 === 0) {
    console.log(`Iteration ${__ITER}: ${scenario.endpoint} (count: ${scenario.params.count}, stringSize: ${scenario.params.stringSize}) - Status: ${response.status}, Duration: ${response.timings.duration.toFixed(2)}ms`);
  }
  
  // Sleep between requests (1-3 seconds)
  sleep(Math.random() * 2 + 1);
}

export function teardown(data) {
  console.log('Load test completed');
  console.log('Check the results above for performance metrics');
}
