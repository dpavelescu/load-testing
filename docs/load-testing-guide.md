# Load Testing Suite Documentation

## Overview

This project includes a comprehensive K6-based load testing suite designed specifically for Kubernetes pod resource sizing and VPA (Vertical Pod Autoscaler) optimization. Each test serves a specific purpose in the resource optimization workflow.

## Test Suite Architecture

### Test Categories

1. **Health & Smoke Tests** (`health-check-test.js`)
2. **Standard Load Tests** (`basic-load-test.js`) 
3. **⚡ Throughput-Based CPU Tests** (`throughput-cpu-calibration.js`) **⭐ ESSENTIAL**
4. **Memory-Focused Tests** (`memory-simulation-test.js`)
5. **Sustained Load Tests** (`sustained-load-test.js`)

---

## Detailed Test Descriptions

### 1. Health Check Test (`health-check-test.js`)

**Purpose**: Quick smoke test and endpoint validation
**Duration**: 30 seconds
**Virtual Users**: 3
**Use Case**: CI/CD pipelines, pre-deployment validation

**What it tests**:
- `/actuator/health` - Overall service health
- `/actuator/health/employeeData` - Custom health indicator
- `/api/employees?count=10&stringSize=128` - Basic employee data generation
- `/api/memory/scenarios` - Memory scenario configuration
- `/api/memory/stats` - JVM memory statistics

**Performance Thresholds**:
- Response time: p(95) < 200ms (very strict)
- Error rate: < 1% (very low tolerance)

**When to use**: 
- Before running other tests
- Quick service validation
- CI/CD pipeline checks
- After deployments

### 2. Basic Load Test (`basic-load-test.js`)

**Purpose**: Standard load testing with realistic user patterns
**Duration**: 4 minutes total
**Load Pattern**: Gradual ramp-up (10 → 20 users)
**Use Case**: General performance validation, resource sizing baseline

**Load Stages**:
1. Ramp up to 10 users (30s)
2. Sustain 10 users (1m)
3. Ramp up to 20 users (30s)
4. Sustain 20 users (2m)
5. Ramp down to 0 (30s)

**Test Scenarios** (randomly selected):
- 50 employees, 256-char strings
- 100 employees, 512-char strings  
- 200 employees, 1024-char strings

**Performance Thresholds**:
- Response time: p(95) < 500ms
- Error rate: < 10%
- Expected throughput: > 50 req/s

**When to use**:
- Initial performance baseline
- After code changes
- Resource sizing validation
- General load capacity testing

### 3. Throughput-Based CPU Calibration Test (`throughput-cpu-calibration.js`) ⭐

**Purpose**: Precise CPU resource calibration through exact throughput control
**Duration**: 10 minutes (configurable)
**Throughput**: Exactly 3 RPS (configurable)
**Use Case**: **CRITICAL for accurate CPU request/limit sizing**

**Why This Test is Essential**:
- **Traditional VU-based tests are unreliable for CPU calibration** - they generate variable throughput
- **VPA needs consistent CPU patterns** to provide accurate recommendations
- **Production environments have predictable traffic patterns** that this test simulates

**Load Pattern**:
- Maintains **exactly 3 requests per second** throughout the test
- Uses K6's `constant-arrival-rate` executor for precision
- No dependency on virtual user calculations or response times

**Request Mix** (Weighted for CPU calibration):
- **60%**: Lightweight operations (25-50 employees, 128-256 char strings)
- **30%**: Moderate operations (100-150 employees, 512-1024 char strings)  
- **10%**: Minimal operations (health checks, stats)

**Performance Thresholds**:
- Response time: p(95) < 1000ms
- Error rate: < 5%
- Throughput validation: ≥90% of expected requests delivered

**Configuration Options**:
```bash
# Default: 3 RPS for 10 minutes
k6 run k6-scripts/throughput-cpu-calibration.js

# Custom throughput and duration
k6 run -e TARGET_RPS=5 -e TEST_DURATION=15m k6-scripts/throughput-cpu-calibration.js

# Light CPU calibration (1 RPS for 15 minutes)
k6 run -e TARGET_RPS=1 -e TEST_DURATION=15m k6-scripts/throughput-cpu-calibration.js

# Heavy CPU calibration (10 RPS for 5 minutes)
k6 run -e TARGET_RPS=10 -e TEST_DURATION=5m k6-scripts/throughput-cpu-calibration.js
```

**When to use**:
- **Before VPA deployment** - establish CPU baseline
- **After resource changes** - validate CPU optimization
- **Production capacity planning** - determine optimal CPU requests
- **Continuous monitoring** - regular CPU calibration checks

**VPA Integration**:
This test is specifically designed for VPA CPU optimization:
1. Generates consistent CPU load patterns
2. Allows VPA to collect reliable CPU usage data
3. Provides predictable baseline for CPU request recommendations
4. Enables accurate CPU limit optimization

### 4. Memory Simulation Test (`memory-simulation-test.js`)

**Purpose**: Memory-focused testing for VPA optimization
**Duration**: 8 minutes total
**Load Pattern**: Memory pressure simulation
**Use Case**: VPA data collection, memory optimization, GC behavior analysis

**Load Stages**:
1. Ramp up to 5 users (1m) - build memory baseline
2. Sustain 5 users (3m) - memory accumulation
3. Spike to 15 users (1m) - memory pressure
4. Sustain 15 users (2m) - peak memory usage
5. Ramp down to 0 (1m) - memory cleanup

**Test Operations** (weighted selection):
- **Memory Scenarios** (80%):
  - Light: Small memory footprint
  - Medium: Moderate memory usage
  - Heavy: High memory consumption
- **Cache Operations** (15%):
  - Cache generation with random data
  - Cache information retrieval
- **Memory Operations** (5%):
  - JVM memory statistics
  - Garbage collection triggers

**Performance Thresholds**:
- Response time: p(95) < 2000ms (slower due to memory ops)
- Error rate: < 15% (higher tolerance for memory pressure)

**When to use**:
- VPA recommendation gathering
- Memory limit optimization
- GC behavior analysis
- Memory leak detection
- Cache performance testing

### 5. Sustained Load Test (`sustained-load-test.js`)

**Purpose**: Long-running stability and endurance testing
**Duration**: 60 minutes total
**Load Pattern**: Extended sustained load with variations
**Use Case**: Production readiness, memory leak detection, stability validation

**Load Stages**:
1. Ramp up to 5 users (2m)
2. Sustain 5 users (15m) - baseline stability
3. Ramp up to 10 users (3m)
4. Sustain 10 users (20m) - peak load stability
5. Scale down to 8 users (3m)
6. Sustain 8 users (15m) - sustained moderate load
7. Ramp down to 0 (2m)

**Test Mix**:
- **90%**: Regular endpoints (health, employees, stats)
- **10%**: Memory simulation calls

**Performance Thresholds**:
- Response time: p(95) < 500ms (consistent performance)
- Error rate: < 10%

**When to use**:
- Production readiness validation
- Memory leak detection
- Long-term stability testing
- Performance degradation analysis
- Before major releases

---

## Testing Strategy for VPA Optimization

### **🎯 Recommended Testing Sequence**

### Phase 1: Health Validation
```bash
k6 run k6-scripts/health-check-test.js
```
- Verify service is healthy
- Confirm all endpoints respond correctly
- Quick validation before heavier testing

### Phase 2: CPU Calibration (CRITICAL) ⭐
```bash
k6 run k6-scripts/throughput-cpu-calibration.js
```
- **Most important test for VPA CPU optimization**
- Generates consistent 3 RPS load for accurate CPU measurement
- Run for 10+ minutes to collect sufficient VPA data
- **Essential for reliable CPU request/limit recommendations**

### Phase 3: Baseline Performance
```bash
k6 run k6-scripts/basic-load-test.js
```
- Establish general performance baseline
- Generate varied load patterns
- Validate current resource allocation

### Phase 4: Memory Optimization
```bash
k6 run k6-scripts/memory-simulation-test.js
```
- Generate comprehensive memory usage patterns
- Trigger different memory scenarios
- Allow VPA to collect memory recommendation data
- **Run this multiple times over 30+ minutes for best VPA data**

### Phase 5: Stability Validation (Optional)
```bash
k6 run k6-scripts/sustained-load-test.js
```
- Validate long-term stability
- Detect memory leaks
- Confirm sustained performance

**Key Insight**: Phase 2 (CPU Calibration) is the most critical for VPA CPU optimization because it provides the consistent load patterns VPA needs for accurate CPU recommendations.

---

## VPA Integration Best Practices

### Pre-VPA Testing
1. Run health check to ensure service stability
2. Run basic load test to establish baseline
3. Document current resource usage

### VPA Data Collection Period
1. Deploy VPA configuration
2. Run memory simulation test 3-5 times over 30-60 minutes
3. Monitor VPA status: `kubectl describe vpa resource-sizing-vpa -n load-testing`
4. Allow VPA recommender to collect sufficient data

### Post-VPA Optimization
1. Apply VPA recommendations to deployment
2. Redeploy service with new resource limits
3. Run full test suite to validate optimization
4. Compare before/after metrics

---

## Monitoring During Tests

### Key Metrics to Track
- **JVM Memory**: `/api/memory/stats`
- **Kubernetes Resources**: `kubectl top pods -n load-testing`
- **VPA Recommendations**: `kubectl describe vpa -n load-testing`
- **Service Health**: `/actuator/health`

### Performance Baselines

| Test | Response Time (p95) | Error Rate | Duration |
|------|-------------------|------------|----------|
| Health Check | < 200ms | < 1% | 30s |
| Basic Load | < 500ms | < 10% | 4m |
| Throughput CPU | < 300ms | 0% throttling | 10m |
| Memory Simulation | < 2000ms | < 15% | 8m |
| Sustained Load | < 500ms | < 10% | 60m |

---

## Troubleshooting

### Common Issues
1. **High Error Rates**: Check service logs and resource constraints
2. **Slow Response Times**: Monitor CPU and memory usage
3. **VPA Not Providing Recommendations**: Ensure sufficient test data collection time
4. **Memory Leaks**: Use sustained load test to detect gradual memory increases

### Log Locations
- **K6 Results**: `k6-scripts/k6-results/`
- **Service Logs**: `kubectl logs -n load-testing deployment/resource-sizing-service`
- **VPA Status**: `kubectl describe vpa resource-sizing-vpa -n load-testing`

This comprehensive test suite provides everything needed for accurate Kubernetes pod resource sizing and VPA optimization validation.

## 🎯 Critical Concept: Virtual Users vs Throughput

### **The Virtual User Problem**
Most load testing tools (including K6) default to "virtual users" (VUs), but this creates a fundamental problem for CPU calibration:

**Virtual Users Generate Variable Throughput**:
- 10 VUs might generate 2 RPS (slow responses) or 20 RPS (fast responses)
- CPU load varies unpredictably based on response times
- VPA receives inconsistent data, making CPU recommendations unreliable

**Example Problem**:
```
Test with 10 VUs:
- Fast responses (100ms): ~100 RPS → High CPU load
- Slow responses (1000ms): ~10 RPS → Low CPU load
- VPA sees inconsistent CPU patterns → Poor recommendations
```

### **The Throughput Solution**
Our approach uses **exact throughput control** instead of virtual users:

**Constant Arrival Rate Benefits**:
- Maintains exactly 3 RPS regardless of response times
- Provides consistent CPU load for accurate VPA data
- Simulates real production traffic patterns
- Enables precise CPU calibration

**Example Solution**:
```
Test with 3 RPS constant rate:
- Fast responses (100ms): Still 3 RPS → Consistent CPU load
- Slow responses (1000ms): Still 3 RPS → Consistent CPU load  
- VPA sees steady CPU patterns → Accurate recommendations
```

### **Mathematical Relationships**

**Traditional VU Formula (Unreliable)**:
```
VUs = Target RPS × Average Response Time + Buffer
Problem: Response time varies → Actual RPS varies → CPU load varies
```

**Throughput-Based Formula (Reliable)**:
```
K6 Constant Arrival Rate:
- rate: 3 (exact requests per second)
- K6 automatically manages VUs to maintain rate
- CPU load remains consistent regardless of response times
```
