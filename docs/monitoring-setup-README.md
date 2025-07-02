# Spring Boot Resource Sizing - Monitoring & Analysis Setup

## Overview

This project provides a complete, research-grade monitoring and performance analysis workflow for Spring Boot applications running on Kubernetes. The setup uses Prometheus Operator and Grafana to collect, visualize, and analyze performance metrics with automated pattern recognition and anomaly detection.

## Architecture

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Spring Boot   │───▶│   Prometheus    │───▶│     Grafana     │
│   Application   │    │   (Operator)    │    │   Dashboard     │
│  + Micrometer   │    │                 │    │                 │
└─────────────────┘    └─────────────────┘    └─────────────────┘
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│  JVM Metrics    │    │  Time Series    │    │  Performance    │
│  HTTP Metrics   │    │     Data        │    │   Analysis      │
│  System Metrics │    │    Storage      │    │  & Reporting    │
└─────────────────┘    └─────────────────┘    └─────────────────┘
```

## Quick Start

### 1. Deploy Monitoring Stack

```bash
# Deploy all monitoring components
kubectl apply -f kubernetes/monitoring/

# Wait for components to be ready
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=prometheus -n monitoring --timeout=300s
kubectl wait --for=condition=ready pod -l app.kubernetes.io/name=grafana -n monitoring --timeout=300s
```

### 2. Access Grafana Dashboard

```bash
# Port forward Grafana
kubectl port-forward svc/grafana 3000:80 -n monitoring

# Open browser to http://localhost:3000
# Default credentials: admin/admin
```

### 3. Import Dashboard

The Grafana dashboard is automatically configured via ConfigMap. If manual import is needed:
- Go to Dashboards → Import
- Upload `kubernetes/monitoring/06-spring-boot-resource-sizing-grafana-dashboard.json`

### 4. Deploy Spring Boot Application

```bash
# Deploy your Spring Boot service with proper annotations
kubectl apply -f kubernetes/app/
```

## Monitoring Components

### Files Structure

```
kubernetes/monitoring/
├── 01-namespace.yaml                    # Monitoring namespace
├── 02-prometheus-cr.yaml               # Prometheus Operator Custom Resource
├── 03-service-monitor.yaml             # ServiceMonitor for metrics scraping
├── 04-grafana.yaml                     # Grafana deployment
├── 05-grafana-datasource.yaml          # Prometheus datasource config
└── 06-spring-boot-resource-sizing-grafana-dashboard.json  # Main dashboard
```

### Key Components

1. **Prometheus Operator**: Manages Prometheus instances using CRDs
2. **ServiceMonitor**: Defines which services to scrape for metrics
3. **Grafana**: Visualization and dashboard platform
4. **Spring Boot Actuator**: Exposes JVM and application metrics

## Dashboard Panels

### Research-Grade Metrics Layout

The dashboard is organized for systematic performance analysis:

| Row | Left Panel | Right Panel |
|-----|------------|-------------|
| 1 | Request Rate (per pod) | Response Time Percentiles |
| 2 | CPU Usage (per pod) | JVM Heap Memory (per pod) |
| 3 | JVM Non-Heap Memory | Active HTTP Requests (total) |
| 4 | JVM Thread Count | Garbage Collection Activity |
| 5 | HTTP Status Codes | Error Rate |
| 6 | JVM Memory Pools | System Load Average |

### Legend Format Standards

All pod-specific metrics use consistent legend formatting:
- **Format**: `Pod-{port}` (e.g., "Pod-8080", "Pod-8081")
- **Memory metrics**: `{type} Pod-{port}` (e.g., "Heap Pod-8080")
- **Thread metrics**: `{category} Pod-{port}` (e.g., "Live Pod-8080")
- **GC metrics**: `{action}.{cause} Pod-{port}` (e.g., "minor.allocation Pod-8080")

This ensures:
- ✅ Unique identification of each pod instance
- ✅ No duplicate or confusing series
- ✅ Clear correlation between metrics and infrastructure
- ✅ Consistent naming across all panels

## Performance Analysis Protocol

### Automated Analysis Workflow

1. **Data Collection**: Prometheus scrapes metrics every 15 seconds
2. **Pattern Recognition**: Grafana visualizes time-series patterns
3. **Correlation Analysis**: Multiple metrics viewed simultaneously
4. **Anomaly Detection**: Visual identification of performance anomalies
5. **Report Generation**: Automated recommendations based on patterns

### Key Analysis Patterns

#### 1. Load Scaling Patterns
- **Request Rate vs CPU**: Linear relationship indicates healthy scaling
- **Memory vs Load**: Heap growth patterns during load increases
- **Thread Count vs Requests**: Connection pool behavior

#### 2. Performance Degradation Signals
- **Response Time Spikes**: Correlate with GC activity or CPU saturation
- **Error Rate Increases**: Often precede resource exhaustion
- **Memory Leaks**: Continuous heap growth without corresponding GC

#### 3. Resource Optimization Indicators
- **CPU Underutilization**: Opportunity for resource reduction
- **Memory Overallocation**: Excessive heap limits vs actual usage
- **Thread Pool Sizing**: Live vs peak thread analysis

### Analysis Checklist for Each Test Run

- [ ] **Load Distribution**: Even request distribution across pods
- [ ] **Response Time Consistency**: P95 < 2x P50 latency
- [ ] **Resource Utilization**: CPU 50-80%, Memory 60-85% of limits
- [ ] **Error Rate**: < 0.1% throughout test duration
- [ ] **GC Impact**: < 5% total execution time
- [ ] **Thread Efficiency**: Live threads scale with load

## Troubleshooting

### Common Issues

#### Metrics Not Appearing
```bash
# Check ServiceMonitor targets
kubectl get servicemonitor -n monitoring -o yaml

# Verify Prometheus targets
kubectl port-forward svc/prometheus-operated 9090:9090 -n monitoring
# Visit http://localhost:9090/targets
```

#### Dashboard Not Loading
```bash
# Check Grafana logs
kubectl logs -l app.kubernetes.io/name=grafana -n monitoring

# Verify datasource connection
# Grafana → Configuration → Data Sources → Test
```

#### Missing Spring Boot Metrics
```bash
# Verify actuator endpoint
kubectl port-forward svc/resource-sizing-service 8080:8080
curl http://localhost:8080/actuator/prometheus

# Check service annotations
kubectl get svc resource-sizing-service -o yaml
```

### Validation Commands

```bash
# Check all monitoring pods
kubectl get pods -n monitoring

# Verify Prometheus is scraping targets
kubectl port-forward svc/prometheus-operated 9090:9090 -n monitoring &
curl -s http://localhost:9090/api/v1/targets | jq '.data.activeTargets[] | select(.labels.job=="resource-sizing-service")'

# Test Grafana dashboard data
kubectl port-forward svc/grafana 3000:80 -n monitoring &
# Check dashboard panels load without errors
```

## Advanced Configuration

### Custom Metrics

Add custom application metrics by extending your Spring Boot application:

```java
@RestController
public class MetricsController {
    
    private final Counter customCounter = Counter.builder("custom_operations_total")
        .description("Total custom operations")
        .register(Metrics.globalRegistry);
    
    private final Timer customTimer = Timer.builder("custom_operation_duration")
        .description("Custom operation duration")
        .register(Metrics.globalRegistry);
}
```

### Alert Rules

Create Prometheus alert rules for automated notifications:

```yaml
apiVersion: monitoring.coreos.com/v1
kind: PrometheusRule
metadata:
  name: spring-boot-alerts
  namespace: monitoring
spec:
  groups:
  - name: spring-boot.rules
    rules:
    - alert: HighErrorRate
      expr: sum(rate(http_server_requests_seconds_count{status=~"5.."}[5m])) / sum(rate(http_server_requests_seconds_count[5m])) > 0.01
      for: 2m
      labels:
        severity: warning
      annotations:
        summary: "High error rate detected"
```

### Dashboard Customization

The dashboard JSON can be modified to add:
- Additional panels for custom metrics
- Different time ranges for specific analysis needs
- Alert annotations for test events
- Variable templating for multi-environment support

## Performance Testing Integration

This monitoring setup integrates seamlessly with K6 load testing:

```javascript
// K6 test script with monitoring validation
import http from 'k6/http';
import { check } from 'k6';

export default function() {
    let response = http.get('http://resource-sizing-service:8080/api/test');
    
    check(response, {
        'status is 200': (r) => r.status === 200,
        'response time < 100ms': (r) => r.timings.duration < 100,
    });
}
```

During test execution, all metrics are automatically collected and visualized in the Grafana dashboard, enabling real-time performance analysis and post-test reporting.

## Next Steps

1. **Baseline Establishment**: Run controlled tests to establish performance baselines
2. **Load Testing**: Execute various load patterns while monitoring metrics
3. **Optimization Cycles**: Use dashboard insights to optimize resource allocation
4. **Continuous Monitoring**: Implement alerting for production environments
5. **Automated Reporting**: Develop scripts to generate performance reports from Prometheus data

For detailed analysis procedures, see `docs/performance-test-analysis-protocol.md`.
