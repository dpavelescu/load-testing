# Kubernetes Deployment Guide

This directory contains Kubernetes manifests and deployment scripts for the Resource Sizing Service.

## Prerequisites

1. **Docker Desktop** with Kubernetes enabled
2. **kubectl** command line tool
3. **Docker image** built locally: `resource-sizing-service:latest`

## Enabling Kubernetes in Docker Desktop

1. Open Docker Desktop
2. Go to Settings → Kubernetes
3. Check "Enable Kubernetes"
4. Click "Apply & Restart"
5. Wait for Kubernetes to start (green indicator)

## Quick Deployment

### Windows
```batch
kubernetes\deploy.bat
```

### Linux/Mac
```bash
chmod +x kubernetes/deploy.sh
./kubernetes/deploy.sh
```

## Manual Deployment

1. **Build the Docker image:**
   ```bash
   docker build -t resource-sizing-service:latest .
   ```

2. **Deploy to Kubernetes:**
   ```bash
   kubectl apply -f kubernetes/app/01-namespace.yaml
   kubectl apply -f kubernetes/app/02-configmap.yaml
   kubectl apply -f kubernetes/app/03-deployment.yaml
   kubectl apply -f kubernetes/app/04-service.yaml
   kubectl apply -f kubernetes/app/05-servicemonitor.yaml
   kubectl apply -f kubernetes/app/06-hpa.yaml
   ```

3. **Wait for deployment:**
   ```bash
   kubectl wait --for=condition=available --timeout=300s deployment/resource-sizing-service -n load-testing
   ```

4. **Access the service:**
   ```bash
   kubectl port-forward service/resource-sizing-service 8080:8080 -n load-testing
   ```

## Verification

Check deployment status:
```bash
kubectl get all -n load-testing
```

Check health:
```bash
curl http://localhost:8080/actuator/health
```

Check metrics:
```bash
curl http://localhost:8080/actuator/prometheus
```

## Resource Configuration

The deployment includes:

- **CPU Requests:** 100m (0.1 CPU cores)
- **CPU Limits:** 500m (0.5 CPU cores)
- **Memory Requests:** 128Mi
- **Memory Limits:** 512Mi
- **HPA:** Auto-scaling from 1-10 replicas based on CPU (70%) and Memory (80%) usage

## Monitoring

The deployment includes:
- **Health checks:** Liveness, readiness, and startup probes
- **Prometheus integration:** Metrics endpoint at `/actuator/prometheus`
- **ServiceMonitor:** For Prometheus operator integration

## Cleanup

### Windows
```batch
kubernetes\cleanup.bat
```

### Linux/Mac
```bash
kubectl delete -f kubernetes/app/
```

## Troubleshooting

1. **Image not found:** Make sure to build the Docker image locally first
2. **Pod not starting:** Check logs with `kubectl logs -n load-testing deployment/resource-sizing-service`
3. **Service not accessible:** Verify port-forward command and firewall settings
4. **Health check failing:** Check application startup logs and configuration

## Files Overview

- `01-namespace.yaml` - Creates the load-testing namespace
- `02-configmap.yaml` - Application configuration
- `03-deployment.yaml` - Main application deployment
- `04-service.yaml` - Service for load balancing
- `05-servicemonitor.yaml` - Prometheus monitoring configuration
- `06-hpa.yaml` - Horizontal Pod Autoscaler configuration
- `deploy.bat/sh` - Automated deployment scripts
- `cleanup.bat` - Cleanup script
