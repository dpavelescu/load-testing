# Monitoring Setup

## Approach

This setup uses the Prometheus Operator to automate service discovery and scraping, and a Grafana deployment to visualize Kubernetes pod and JVM metrics for Spring Boot resource sizing tests.

### Components & Files
1. **01-namespace.yaml**: Creates the `monitoring` namespace.
2. **02-prometheus-cr.yaml**: Defines a Prometheus instance via the Prometheus Operator.
3. **03-service-monitor.yaml**: ServiceMonitor for scraping the Spring Boot `/actuator/prometheus` endpoint.
4. **04-grafana.yaml**: Deploys Grafana and exposes it.
5. **05-grafana-datasource.yaml**: ConfigMap that provisions the Prometheus datasource for Grafana.
6. **06-spring-boot-resource-sizing-grafana-dashboard.json**: Grafana dashboard JSON file for import.

## Component Ordering and Naming

All monitoring manifests are prefixed with an index to define deployment order:

- **01-namespace.yaml**: Create `monitoring` namespace.
- **02-prometheus-cr.yaml**: Operator-managed Prometheus instance.
- **03-service-monitor.yaml**: ServiceMonitor for Spring Boot metrics.
- **04-grafana.yaml**: Grafana Deployment & Service.
- **05-grafana-datasource.yaml**: Provision Prometheus datasource for Grafana.
- **06-spring-boot-resource-sizing-grafana-dashboard.json**: Grafana dashboard JSON to import.

Note: There is only one Grafana deployment (`04-grafana.yaml`) and one datasource config (`05-grafana-datasource.yaml`). Ensure file prefixes match this ordering.

## Quick Setup

```cmd
# Deploy monitoring resources
kubectl apply -f kubernetes/monitoring/

# Port-forward Prometheus
kubectl port-forward svc/k8s-prometheus-operated -n monitoring 9090:9090

# Port-forward Grafana
kubectl port-forward svc/grafana -n monitoring 30030:3000
```

Open:
- Prometheus UI at http://localhost:9090
- Grafana UI at http://localhost:30030

## Next Steps
1. Import the dashboard JSON into Grafana (via UI).
2. Run K6 load tests to generate traffic (see `k6-scripts/`).
3. Verify Grafana panels display CPU, memory, JVM, and HTTP metrics.
4. Tune ServiceMonitor intervals or dashboard queries if needed.
