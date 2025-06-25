import http from 'k6/http';
import { check, sleep } from 'k6';

// Quick health check and smoke test
export const options = {
  vus: 3,          // 3 virtual users
  duration: '30s', // Run for 30 seconds
  thresholds: {
    http_req_duration: ['p(95)<200'],  // Fast response time required
    http_req_failed: ['rate<0.01'],    // Very low error rate
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export default function () {
  // Test all critical endpoints
  const endpoints = [
    '/actuator/health',
    '/actuator/health/employeeData',
    '/api/employees?count=10&stringSize=128',
    '/api/memory/scenarios',
    '/api/memory/stats',
  ];
  
  const endpoint = endpoints[__ITER % endpoints.length];
  const response = http.get(`${BASE_URL}${endpoint}`, {
    headers: { 'Accept': 'application/json' },
    timeout: '10s',
  });
  
  const endpointName = endpoint.split('?')[0]; // Remove query params for check names
  
  check(response, {
    [`${endpointName} status is 200`]: (r) => r.status === 200,
    [`${endpointName} response time < 200ms`]: (r) => r.timings.duration < 200,
    [`${endpointName} has content`]: (r) => r.body.length > 0,
  });
  
  // Log any failures immediately
  if (response.status !== 200) {
    console.error(`FAILURE: ${endpoint} returned ${response.status}`);
  }
  
  sleep(1);
}
