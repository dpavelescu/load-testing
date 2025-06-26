import http from 'k6/http';
import { check, sleep } from 'k6';

export let options = {
  stages: [
    { duration: '2m', target: 5 },    // Ramp up to 5 users over 2 minutes
    { duration: '15m', target: 5 },   // Stay at 5 users for 15 minutes
    { duration: '3m', target: 10 },   // Ramp up to 10 users over 3 minutes
    { duration: '20m', target: 10 },  // Stay at 10 users for 20 minutes
    { duration: '3m', target: 8 },    // Scale down to 8 users over 3 minutes
    { duration: '15m', target: 8 },   // Stay at 8 users for 15 minutes
    { duration: '2m', target: 0 },    // Ramp down to 0 users over 2 minutes
  ],
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% of requests must complete below 500ms
    http_req_failed: ['rate<0.1'],    // Less than 10% of requests should fail
  },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
  // Test different endpoints with varying loads
  let endpoints = [
    '/actuator/health',
    '/api/employees',
    '/api/employees/count',
    '/actuator/health/employeeData',
    '/api/memory/stats',
  ];
  
  // Randomly select an endpoint
  let endpoint = endpoints[Math.floor(Math.random() * endpoints.length)];
  
  let response = http.get(`${BASE_URL}${endpoint}`);
  
  check(response, {
    'status is 200': (r) => r.status === 200,
    'response time < 500ms': (r) => r.timings.duration < 500,
  });
  
  // Add memory simulation calls occasionally (10% of requests)
  if (Math.random() < 0.1) {
    let memoryScenario = Math.random() < 0.5 ? 'SMALL_OBJECTS' : 'MEDIUM_OBJECTS';
    let memoryResponse = http.post(`${BASE_URL}/api/memory/simulate`, 
      JSON.stringify({ scenario: memoryScenario }), 
      { headers: { 'Content-Type': 'application/json' } }
    );
    
    check(memoryResponse, {
      'memory simulation status is 200': (r) => r.status === 200,
    });
  }
  
  // Variable sleep time between 1-3 seconds
  sleep(Math.random() * 2 + 1);
}
