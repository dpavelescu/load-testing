# Monitoring Setup

## Simplified Approach

This monitoring setup is **purposefully minimal** and focused solely on resource sizing:

✅ **What we DO have:**
- Minimal Prometheus (single container)
- Direct Spring Boot metrics scraping
- Essential container metrics
- Simple query interface
- No passwords or complex configuration

❌ **What we deliberately AVOID:**
- Grafana (overengineered for resource sizing)
- AlertManager (not needed for testing)
- Node Exporter (fails on Docker Desktop)
- Kube State Metrics (overkill for single-app testing)
- Prometheus Operator (adds unnecessary complexity)

## Quick Setup

```bash
# Deploy monitoring
kubectl apply -f kubernetes/monitoring/

# Access Prometheus
kubernetes/monitoring/access-monitoring.bat

# Run load tests and observe metrics
k6 run k6-scripts/basic-load-test.js
```

## Key Metrics to Watch

1. **CPU Usage**: `rate(container_cpu_usage_seconds_total{pod=~"resource-sizing-service.*"}[1m])`
2. **Memory Usage**: `container_memory_working_set_bytes{pod=~"resource-sizing-service.*"}`
3. **JVM Memory**: `jvm_memory_used_bytes{application="resource-sizing-service"}`
4. **HTTP Throughput**: `rate(http_server_requests_total{application="resource-sizing-service"}[1m])`

## Resource Sizing Methodology

1. **Baseline**: Run with minimal resources (100m CPU, 128Mi memory)
2. **Load Test**: Apply various load patterns
3. **Observe**: CPU throttling, memory pressure, GC pressure
4. **Adjust**: Increase resources based on observed bottlenecks
5. **Validate**: Confirm performance improvement
6. **Document**: Final recommendations

## No Sidecars Needed!

- Spring Boot exposes metrics directly via Actuator
- Prometheus scrapes via service discovery
- No additional containers per pod
- Minimal resource overhead
