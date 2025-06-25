# K6 Load Testing Configuration

## Environment Variables

# Service URL (default: http://localhost:8080)
BASE_URL=http://localhost:8080

# Test output directory
K6_OUT_DIR=./k6-results

## Test Scripts

1. **health-check-test.js** - Quick smoke test (30 seconds)
   - Tests all critical endpoints
   - Low load, fast response requirements
   - Good for CI/CD pipelines

2. **basic-load-test.js** - Standard load test (4 minutes)
   - Gradual ramp-up with different employee data scenarios
   - Tests response times and error rates
   - Simulates realistic user behavior

3. **memory-simulation-test.js** - Memory-focused test (8 minutes)
   - Tests memory scenarios and cache operations
   - Longer duration for memory pressure testing
   - Includes garbage collection and memory statistics

## Running Tests

### Prerequisites
- Install k6: https://k6.io/docs/getting-started/installation/
- Ensure the Spring Boot service is running

### Commands

```bash
# Quick health check
k6 run k6-scripts/health-check-test.js

# Basic load test
k6 run k6-scripts/basic-load-test.js

# Memory simulation test
k6 run k6-scripts/memory-simulation-test.js

# With custom base URL
k6 run -e BASE_URL=http://my-service:8080 k6-scripts/basic-load-test.js

# Save results to file
k6 run --out json=results.json k6-scripts/basic-load-test.js

# Run with summary output
k6 run --summary-export=summary.json k6-scripts/basic-load-test.js
```

## Expected Performance Baselines

### Health Check Test
- Response time: p(95) < 200ms
- Error rate: < 1%

### Basic Load Test
- Response time: p(95) < 500ms
- Error rate: < 10%
- Throughput: > 50 requests/second

### Memory Simulation Test
- Response time: p(95) < 2000ms (memory operations are slower)
- Error rate: < 15%
- Memory usage should be observable in metrics

## Monitoring During Tests

1. **Actuator Endpoints:**
   - `/actuator/health` - Overall service health
   - `/actuator/metrics` - JVM and application metrics
   - `/actuator/prometheus` - Prometheus-formatted metrics

2. **Memory Endpoints:**
   - `/api/memory/stats` - Current JVM memory usage
   - `/api/memory/cache/info` - Cache statistics

3. **Docker Stats:**
   ```bash
   docker stats resource-sizing-service
   ```

## Interpreting Results

- **http_req_duration**: Request response time
- **http_req_failed**: Percentage of failed requests
- **http_reqs**: Total number of requests
- **vus**: Current number of virtual users
- **iterations**: Total test iterations completed
