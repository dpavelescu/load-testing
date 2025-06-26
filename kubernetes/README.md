# Kubernetes Deployment Guide

This directory contains Kubernetes manifests for the Resource Sizing Service with VPA (Vertical Pod Autoscaler) integration.

## Prerequisites

1. **Docker Desktop** with Kubernetes enabled
2. **kubectl** command line tool
3. **Helm** package manager
4. **Docker image** built locally: `resource-sizing-service:latest`

## Directory Structure

```
kubernetes/
├── app/                    # Application manifests
│   ├── 01-namespace.yaml   # Load testing namespace
│   ├── 02-configmap.yaml   # Application configuration
│   ├── 03-deployment.yaml  # Spring Boot deployment
│   ├── 04-service.yaml     # Kubernetes service
│   └── 05-hpa-template.yaml # HPA template (for future use)
├── monitoring/             # Prometheus monitoring setup
├── vpa/                   # VPA configuration
│   ├── 04-resource-sizing-vpa.yaml # VPA definition
│   ├── vpa-values.yaml    # Helm values
│   └── README.md          # VPA setup guide
├── cleanup.bat           # Cleanup script
└── README.md            # This file
```

## Quick Deployment

1. **Build and deploy the application:**
   ```bash
   # Build Docker image
   docker build -t resource-sizing-service:latest .
   
   # Deploy application
   kubectl apply -f kubernetes/app/
   ```

2. **Install VPA using Helm:**
   ```bash
   helm repo add fairwinds-stable https://charts.fairwinds.com/stable
   helm install vpa fairwinds-stable/vpa --namespace vpa-system --create-namespace \
     --set recommender.resources.requests.memory=200Mi \
     --set recommender.resources.limits.memory=300Mi \
     --set updater.resources.requests.memory=150Mi \
     --set updater.resources.limits.memory=200Mi \
     --set admissionController.resources.requests.memory=100Mi \
     --set admissionController.resources.limits.memory=150Mi
   ```

3. **Deploy VPA configuration:**
   ```bash
   kubectl apply -f kubernetes/vpa/04-resource-sizing-vpa.yaml
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
