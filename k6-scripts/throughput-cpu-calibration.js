import http from 'k6/http';
import { check, sleep } from 'k6';
import { Rate, Counter, Trend } from 'k6/metrics';

// Custom metrics for throughput analysis
const errorRate = new Rate('errors');
const throughputMetric = new Counter('requests_sent');
const responseTime = new Trend('response_time_trend');

// Configuration - can be overridden with environment variables
const TARGET_RPS = parseFloat(__ENV.TARGET_RPS || '3'); // Default 3 requests per second
const TEST_DURATION = __ENV.TEST_DURATION || '10m';     // Default 10 minutes
const RAMP_DURATION = __ENV.RAMP_DURATION || '30s';     // Ramp up/down time

// VU Pool Sizing Explanation:
// - preAllocatedVUs: Always ready VUs (TARGET_RPS * 2 for fast responses)
// - maxVUs: Maximum VUs if responses are slow (TARGET_RPS * 5 for safety)
// - Actual active VUs: Varies based on response times (0 to maxVUs)
// - This ensures EXACT throughput regardless of response time variations

// Calculate virtual users needed for target RPS
// Formula: VUs = (Target RPS × Average Response Time) + buffer
// For 3 RPS with ~200ms response time: VUs = (3 × 0.2) + 1 = ~2 VUs
// We'll use arrival rate instead for precise control
export const options = {
  scenarios: {
    constant_throughput: {
      executor: 'constant-arrival-rate',
      rate: TARGET_RPS,                    // Exact requests per second
      timeUnit: '1s',                      // Per second
      duration: TEST_DURATION,             // Total test duration
      preAllocatedVUs: Math.ceil(TARGET_RPS * 2), // Pre-allocate enough VUs
      maxVUs: Math.ceil(TARGET_RPS * 5),   // Maximum VUs if needed
      gracefulStop: RAMP_DURATION,         // Graceful shutdown (correct field name)
    },
  },
  thresholds: {
    // Throughput validation
    'http_req_duration': ['p(95)<2000'],   // 95% of requests under 2s (allowing for DB latency)
    'http_req_failed': ['rate<0.05'],      // Less than 5% errors
    'checks': ['rate>0.95'],               // 95% of checks should pass
    
    // Custom thresholds - simplified for K6 compatibility
    'requests_sent': ['count>10'],         // At least 10 requests sent
  },
};

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// Parse duration string to seconds (e.g., "10m" -> 600)
function parseTime(duration) {
  const match = duration.match(/^(\d+)([smh])$/);
  if (!match) return 600; // Default 10 minutes
  
  const value = parseInt(match[1]);
  const unit = match[2];
  
  switch (unit) {
    case 's': return value;
    case 'm': return value * 60;
    case 'h': return value * 3600;
    default: return 600;
  }
}

export function setup() {
  console.log('=== Throughput-Based CPU Calibration Test (with DB Latency) ===');
  console.log(`Target RPS: ${TARGET_RPS}`);
  console.log(`Test Duration: ${TEST_DURATION}`);
  console.log(`Expected Total Requests: ${TARGET_RPS * parseTime(TEST_DURATION)}`);
  console.log(`Base URL: ${BASE_URL}`);
  console.log('Database Latency: 50ms (fast) and 150ms (typical) queries enabled');
  console.log('');
  
  // Health check
  const healthCheck = http.get(`${BASE_URL}/actuator/health`);
  check(healthCheck, {
    'Health check passed': (r) => r.status === 200,
    'Service is UP': (r) => r.json('status') === 'UP',
  });
  
  if (healthCheck.status !== 200) {
    throw new Error('Service health check failed - aborting test');
  }
  
  // Warm up JVM (including DB latency endpoint)
  console.log('Warming up JVM and DB latency simulation...');
  for (let i = 0; i < 5; i++) {
    http.get(`${BASE_URL}/api/employees?count=50&stringSize=256`);
  }
  for (let i = 0; i < 3; i++) {
    http.get(`${BASE_URL}/api/load-test/db-latency?delayMs=100&count=15`);
  }
  console.log('Warm-up completed');
  
  return { startTime: Date.now() };
}

export default function () {
  const startTime = Date.now();
  
  // Test scenarios for CPU calibration WITH realistic database latency
  // Mix of lightweight, moderate CPU operations, and database simulation
  const scenarios = [
    // Lightweight CPU operations (40%)
    { 
      endpoint: '/api/employees', 
      params: { count: 25, stringSize: 128 },
      weight: 0.2 
    },
    { 
      endpoint: '/api/employees', 
      params: { count: 50, stringSize: 256 },
      weight: 0.2 
    },
    
    // Moderate CPU operations (20%)
    { 
      endpoint: '/api/employees', 
      params: { count: 100, stringSize: 512 },
      weight: 0.15 
    },
    { 
      endpoint: '/api/employees', 
      params: { count: 150, stringSize: 1024 },
      weight: 0.05 
    },
    
    // Database latency simulation (20%) - realistic DB operations
    { 
      endpoint: '/api/load-test/db-latency', 
      params: { delayMs: 50, count: 10 },   // Fast DB query (50ms)
      weight: 0.1 
    },
    { 
      endpoint: '/api/load-test/db-latency', 
      params: { delayMs: 150, count: 25 },  // Typical DB query (150ms)
      weight: 0.1 
    },
    
    // Health and stats endpoints (10%) - minimal CPU
    { 
      endpoint: '/actuator/health', 
      params: {},
      weight: 0.05 
    },
    { 
      endpoint: '/api/memory/stats', 
      params: {},
      weight: 0.05 
    },
  ];
  
  // Weighted random selection
  const scenario = weightedRandomSelect(scenarios);
  
  // Build request URL
  let url = `${BASE_URL}${scenario.endpoint}`;
  if (Object.keys(scenario.params).length > 0) {
    const params = Object.keys(scenario.params)
      .map(key => `${key}=${scenario.params[key]}`)
      .join('&');
    url += `?${params}`;
  }
  
  // Make the request
  const response = http.get(url, {
    headers: {
      'Accept': 'application/json',
      'User-Agent': 'k6-throughput-test/1.0',
    },
    timeout: '30s',
  });
  
  const requestTime = Date.now() - startTime;
  
  // Validate response
  const result = check(response, {
    'Status is 200': (r) => r.status === 200,
    'Response time acceptable': (r) => r.timings.duration < 2000,
    'Response has content': (r) => r.body && r.body.length > 0,
  });
  
  // Record metrics
  errorRate.add(!result);
  throughputMetric.add(1);
  responseTime.add(response.timings.duration);
  
  // Log detailed info every 30 seconds
  if (__ITER > 0 && __ITER % (TARGET_RPS * 30) === 0) {
    console.log(`[${Math.floor(__ITER / TARGET_RPS)}s] Requests: ${__ITER}, Current RPS: ~${TARGET_RPS}, Response: ${response.timings.duration.toFixed(0)}ms, Status: ${response.status}`);
  }
  
  // No sleep() needed - arrival rate controls the timing
}

function weightedRandomSelect(items) {
  const totalWeight = items.reduce((sum, item) => sum + item.weight, 0);
  let random = Math.random() * totalWeight;
  
  for (const item of items) {
    random -= item.weight;
    if (random <= 0) {
      return item;
    }
  }
  
  return items[0]; // Fallback
}

export function teardown(data) {
  console.log('\n=== Throughput Test Completed ===');
  
  // Calculate actual test duration
  const actualDuration = (Date.now() - data.startTime) / 1000;
  const expectedRequests = TARGET_RPS * parseTime(TEST_DURATION);
  
  console.log(`Target RPS: ${TARGET_RPS}`);
  console.log(`Test Duration: ${actualDuration.toFixed(1)}s`);
  console.log(`Expected Requests: ${expectedRequests}`);
  
  // Get final memory stats
  const memStats = http.get(`${BASE_URL}/api/memory/stats`);
  if (memStats.status === 200) {
    try {
      const stats = memStats.json();
      console.log('\n=== Final Resource Usage ===');
      console.log(`Memory Usage: ${stats.memoryUsagePercentage}%`);
      console.log(`Used Memory: ${stats.usedMemoryMB} MB`);
      console.log(`Max Memory: ${stats.maxMemoryMB} MB`);
    } catch (e) {
      console.log('Could not retrieve final memory stats');
    }
  }
  
  console.log('\n=== CPU Calibration Notes ===');
  console.log(`This test maintains exactly ${TARGET_RPS} RPS for CPU measurement`);
  console.log('Monitor CPU usage with: kubectl top pods -n load-testing');
  console.log('Monitor VPA recommendations: kubectl describe vpa resource-sizing-vpa -n load-testing');
}
