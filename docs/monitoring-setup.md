# Monitoring Setup Guide

## Overview

This guide covers the simplified monitoring setup for Kubernetes pod resource sizing. Our approach deliberately avoids overengineering and focuses solely on collecting the metrics needed for accurate resource recommendations.

## Philosophy: Simplified vs. Overengineered

### ❌ What We Avoided (Overengineered)
- **Grafana**: Complex dashboards with datasource conflicts
- **AlertManager**: Alerting not needed for resource sizing tests
- **Node Exporter**: Fails on Docker Desktop, not essential for pod metrics
- **Kube State Metrics**: Cluster-wide metrics overkill for single-app testing
- **Prometheus Operator**: CRDs and operators add unnecessary complexity
- **Sidecar containers**: Additional resource overhead per pod

### ✅ What We Implemented (Focused)
- **Minimal Prometheus**: Single container, essential metrics only
- **Direct scraping**: Spring Boot Actuator metrics via service discovery
- **No passwords**: Simple access without credential management
- **Pure YAML**: No Helm charts or complex dependencies
- **Resource-focused**: Only CPU, memory, and JVM metrics that matter

## Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│  Spring Boot    │    │   Prometheus     │    │   Developer     │
│  Application    │───▶│   (monitoring    │───▶│   (localhost    │
│  (load-testing) │    │    namespace)    │    │    :9090)       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
      │ Actuator              │ Scrapes                │ Queries
      │ /actuator/prometheus  │ every 5s              │ PromQL
      └───────────────────────┘                       └─────────
```

## Setup Instructions

### 1. Deploy Monitoring

```bash
# From the project root
kubectl apply -f kubernetes/monitoring/

# Verify deployment
kubectl get pods -n monitoring
# Should show: prometheus-xxx   1/1   Running
```

*Note: For basic project setup, see the main [README.md](../README.md)*

### 2. Access Prometheus UI

**Option A: Using the provided script (recommended)**
```bash
cd kubernetes\monitoring
access-monitoring.bat
# This will open http://localhost:9090 automatically
```

**Option B: Manual port forwarding**
```bash
kubectl port-forward -n monitoring svc/prometheus 9090:9090
# Then open http://localhost:9090 in your browser
```

### 3. Verify Metrics Collection

In the Prometheus UI (http://localhost:9090):

1. **Check Targets**: Go to Status > Targets
   - Should see `resource-sizing-service` target as UP
   
2. **Test Queries**: Go to Graph tab and try:
   ```promql
   # Check if Spring Boot metrics are available
   up{job="resource-sizing-service"}
   
   # JVM memory usage
   jvm_memory_used_bytes{application="resource-sizing-service"}
   
   # HTTP request rate
   rate(http_server_requests_total[1m])
   ```

## Key Metrics for Resource Sizing

### CPU Metrics
```promql
# CPU usage rate (most important for CPU sizing)
rate(container_cpu_usage_seconds_total{pod=~"resource-sizing-service.*"}[1m])

# CPU throttling (indicates CPU limits are too low)
rate(container_cpu_cfs_throttled_seconds_total{pod=~"resource-sizing-service.*"}[1m])
```

### Memory Metrics
```promql
# Working set memory (used for OOM decisions)
container_memory_working_set_bytes{pod=~"resource-sizing-service.*"}

# Memory usage percentage
(container_memory_working_set_bytes{pod=~"resource-sizing-service.*"} / container_spec_memory_limit_bytes{pod=~"resource-sizing-service.*"}) * 100
```

### JVM-Specific Metrics
```promql
# Heap memory used
jvm_memory_used_bytes{application="resource-sizing-service", area="heap"}

# GC time (indicates memory pressure)
rate(jvm_gc_collection_seconds_sum{application="resource-sizing-service"}[1m])

# GC frequency
rate(jvm_gc_collection_seconds_count{application="resource-sizing-service"}[1m])
```

### Application Metrics
```promql
# HTTP request rate
rate(http_server_requests_total{application="resource-sizing-service"}[1m])

# Request duration
histogram_quantile(0.95, rate(http_server_requests_duration_seconds_bucket{application="resource-sizing-service"}[1m]))

# Error rate
rate(http_server_requests_total{application="resource-sizing-service", status=~"5.."}[1m])
```

## Resource Sizing Methodology

### 1. Baseline Measurement
- Deploy with minimal resources (100m CPU, 128Mi memory)
- Run basic load test to establish baseline metrics
- Record normal operation metrics

### 2. Load Testing Phases
Run different load patterns while monitoring (see main README for basic commands):
- `basic-load-test.js` - Steady load patterns
- `memory-simulation-test.js` - Memory pressure testing  
- `health-check-test.js` - Health endpoint validation

### 3. Analysis Approach
1. **Identify Bottlenecks**: Look for CPU throttling or memory pressure
2. **Correlate Metrics**: Match resource usage spikes with load patterns
3. **Calculate Headroom**: Add 20-30% buffer to peak usage
4. **Test Adjustments**: Deploy with new limits and retest
5. **Document Results**: Record recommendations with justification

### 4. Decision Matrix

| Metric | Threshold | Action |
|--------|-----------|--------|
| CPU usage > 80% | Increase CPU limits |
| CPU throttling > 0 | Increase CPU limits |
| Memory usage > 80% of limit | Increase memory limits |
| GC time > 10ms avg | Increase heap size/memory |
| Response time P95 > 200ms | Investigate CPU/memory |

## Troubleshooting

### Prometheus Not Scraping Application
```bash
# Check if application is exposing metrics
kubectl port-forward -n load-testing svc/resource-sizing-service 8080:8080
# Visit http://localhost:8080/actuator/prometheus

# Check Prometheus config
kubectl get configmap prometheus-config -n monitoring -o yaml

# Check Prometheus logs
kubectl logs -n monitoring deployment/prometheus
```

### Port Forward Issues
```bash
# Kill existing port forwards
tasklist | findstr kubectl
taskkill /F /PID <pid>

# Or use different port
kubectl port-forward -n monitoring svc/prometheus 9091:9090
```

### No Container Metrics
Container metrics come from cAdvisor and may not be available in all Docker Desktop setups. Focus on JVM and application metrics which are always available via Spring Boot Actuator.

## Security Considerations

### Current Setup (Development)
- No authentication required for Prometheus access
- Port forwarding exposes metrics only to localhost
- Suitable for local development and testing

### Production Considerations (Future)
- Enable Prometheus authentication
- Use ingress controllers instead of port forwarding  
- Implement RBAC for monitoring namespace
- Consider metrics retention policies

## Next Steps

After monitoring setup is complete:

1. **Run Load Tests**: Execute K6 scripts while monitoring metrics
2. **Analyze Results**: Use the key metrics to identify resource bottlenecks
3. **Implement VPA**: Set up Vertical Pod Autoscaler for automated recommendations
4. **Document Findings**: Create resource sizing recommendations based on empirical data

## Files Overview

```
kubernetes/monitoring/
├── 01-namespace.yaml          # Monitoring namespace
├── 02-prometheus.yaml         # Prometheus deployment & service
├── 03-prometheus-config.yaml  # Prometheus scrape configuration
├── access-monitoring.bat      # Windows script for easy access
├── access-monitoring.sh       # Linux/Mac script for easy access
└── README.md                  # Quick reference guide
```

This simplified approach gives us exactly what we need for accurate resource sizing without the complexity and overhead of a full monitoring stack.
