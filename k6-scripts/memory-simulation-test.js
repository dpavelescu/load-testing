import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter } from 'k6/metrics';

// Custom metrics
const errorRate = new Rate('errors');
const cacheOperations = new Counter('cache_operations');
const memoryOperations = new Counter('memory_operations');

// Test configuration for memory simulation testing
export const options = {
  stages: [
    { duration: '1m', target: 5 },     // Ramp up to 5 users over 1 minute
    { duration: '3m', target: 5 },     // Stay at 5 users for 3 minutes (memory build-up)
    { duration: '1m', target: 15 },    // Spike to 15 users over 1 minute
    { duration: '2m', target: 15 },    // Stay at 15 users for 2 minutes
    { duration: '1m', target: 0 },     // Ramp down to 0 users
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'],  // 95% of requests should be below 2 seconds (memory ops are slower)
    http_req_failed: ['rate<0.15'],     // Error rate should be less than 15%
    errors: ['rate<0.15'],
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export function setup() {
  // Health check
  const healthCheck = http.get(`${BASE_URL}/actuator/health`);
  check(healthCheck, {
    'Health check status is 200': (r) => r.status === 200,
    'Service is UP': (r) => r.json('status') === 'UP',
  });
  
  // Clear any existing cache before starting
  const clearResponse = http.del(`${BASE_URL}/api/memory/cache`);
  console.log(`Cleared cache before test: ${clearResponse.status}`);
  
  console.log(`Starting memory simulation test against: ${BASE_URL}`);
}

export default function () {
  const scenarios = [
    // Memory scenario tests
    { type: 'scenario', scenario: 'light' },
    { type: 'scenario', scenario: 'medium' },
    { type: 'scenario', scenario: 'heavy' },
    
    // Cache operations
    { type: 'cache', operation: 'generate' },
    { type: 'cache', operation: 'info' },
    
    // Memory statistics
    { type: 'memory', operation: 'stats' },
    { type: 'memory', operation: 'gc' },
  ];
  
  // Weighted random selection (favor memory scenarios)
  const weights = [0.3, 0.3, 0.2, 0.1, 0.05, 0.03, 0.02];
  let random = Math.random();
  let selectedIndex = 0;
  
  for (let i = 0; i < weights.length; i++) {
    random -= weights[i];
    if (random <= 0) {
      selectedIndex = i;
      break;
    }
  }
  
  const scenario = scenarios[selectedIndex];
  let response;
  
  switch (scenario.type) {
    case 'scenario':
      response = testMemoryScenario(scenario.scenario);
      break;
    case 'cache':
      response = testCacheOperation(scenario.operation);
      break;
    case 'memory':
      response = testMemoryOperation(scenario.operation);
      break;
  }
  
  // Validate response
  const result = check(response, {
    'Status is 2xx': (r) => r.status >= 200 && r.status < 300,
    'Response time < 3000ms': (r) => r.timings.duration < 3000,
    'No server errors': (r) => r.status < 500,
  });
  
  errorRate.add(!result);
  
  // Log performance metrics occasionally
  if (__ITER % 15 === 0) {
    console.log(`Iteration ${__ITER}: ${scenario.type}/${scenario.scenario || scenario.operation} - Status: ${response.status}, Duration: ${response.timings.duration.toFixed(2)}ms`);
  }
  
  // Variable sleep to simulate real user behavior
  sleep(Math.random() * 3 + 1);
}

function testMemoryScenario(scenarioName) {
  const url = `${BASE_URL}/api/memory/scenario/${scenarioName}`;
  const response = http.get(url, {
    headers: { 'Accept': 'application/json' },
    timeout: '45s',
  });
  
  memoryOperations.add(1);
  
  if (response.status === 200) {
    check(response, {
      [`${scenarioName} scenario returns array`]: (r) => {
        try {
          const body = r.json();
          return Array.isArray(body);
        } catch (e) {
          return false;
        }
      },
    });
  }
  
  return response;
}

function testCacheOperation(operation) {
  let response;
  
  switch (operation) {
    case 'generate':
      const cacheKey = `test-${__VU}-${__ITER}`;
      const count = Math.floor(Math.random() * 200) + 50; // 50-250 employees
      const stringSize = [256, 512, 1024, 2048][Math.floor(Math.random() * 4)];
      
      response = http.post(`${BASE_URL}/api/memory/cache`, null, {
        params: {
          cacheKey: cacheKey,
          count: count,
          stringSize: stringSize,
        },
        headers: { 'Accept': 'application/json' },
        timeout: '60s',
      });
      
      cacheOperations.add(1);
      break;
      
    case 'info':
      response = http.get(`${BASE_URL}/api/memory/cache/info`, {
        headers: { 'Accept': 'application/json' },
        timeout: '10s',
      });
      break;
  }
  
  return response;
}

function testMemoryOperation(operation) {
  let response;
  
  switch (operation) {
    case 'stats':
      response = http.get(`${BASE_URL}/api/memory/stats`, {
        headers: { 'Accept': 'application/json' },
        timeout: '10s',
      });
      
      if (response.status === 200) {
        check(response, {
          'Memory stats contain expected fields': (r) => {
            try {
              const stats = r.json();
              return stats.usedMemoryBytes !== undefined && stats.maxMemoryBytes !== undefined;
            } catch (e) {
              return false;
            }
          },
        });
      }
      break;
      
    case 'gc':
      response = http.post(`${BASE_URL}/api/memory/gc`, null, {
        headers: { 'Accept': 'application/json' },
        timeout: '15s',
      });
      break;
  }
  
  return response;
}

export function teardown(data) {
  // Get final memory statistics
  const finalStats = http.get(`${BASE_URL}/api/memory/stats`);
  if (finalStats.status === 200) {
    try {
      const stats = finalStats.json();
      console.log('=== Final Memory Statistics ===');
      console.log(`Used Memory: ${stats.usedMemoryMB}`);
      console.log(`Max Memory: ${stats.maxMemoryMB}`);
      console.log(`Memory Usage: ${stats.memoryUsagePercentage}`);
    } catch (e) {
      console.log('Could not parse final memory statistics');
    }
  }
  
  // Clear cache after test
  const clearResponse = http.del(`${BASE_URL}/api/memory/cache`);
  console.log(`Cleared cache after test: ${clearResponse.status}`);
  
  console.log('Memory simulation test completed');
}
