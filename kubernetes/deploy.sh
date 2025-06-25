#!/bin/bash
# Deploy the application to Kubernetes

set -e

echo "🚀 Deploying Resource Sizing Service to Kubernetes..."

# Check if kubectl is available
if ! command -v kubectl &> /dev/null; then
    echo "❌ kubectl is not installed or not in PATH"
    exit 1
fi

# Check if Docker Desktop Kubernetes is running
if ! kubectl cluster-info &> /dev/null; then
    echo "❌ Kubernetes cluster is not accessible. Make sure Docker Desktop Kubernetes is enabled."
    exit 1
fi

echo "✅ Kubernetes cluster is accessible"

# Build the Docker image
echo "🏗️ Building Docker image..."
docker build -t resource-sizing-service:latest .

echo "✅ Docker image built successfully"

# Apply Kubernetes manifests
echo "📦 Applying Kubernetes manifests..."

# Create namespace first
kubectl apply -f kubernetes/app/01-namespace.yaml

# Apply all other manifests
kubectl apply -f kubernetes/app/02-configmap.yaml
kubectl apply -f kubernetes/app/03-deployment.yaml
kubectl apply -f kubernetes/app/04-service.yaml
kubectl apply -f kubernetes/app/05-servicemonitor.yaml
kubectl apply -f kubernetes/app/06-hpa.yaml

echo "✅ All manifests applied successfully"

# Wait for deployment to be ready
echo "⏳ Waiting for deployment to be ready..."
kubectl wait --for=condition=available --timeout=300s deployment/resource-sizing-service -n load-testing

echo "✅ Deployment is ready!"

# Show status
echo "📊 Current status:"
kubectl get pods -n load-testing
kubectl get services -n load-testing
kubectl get hpa -n load-testing

# Get service URL
echo ""
echo "🌐 Service endpoints:"
kubectl get service resource-sizing-service -n load-testing -o jsonpath='{.spec.clusterIP}'
echo ""
echo "To access the service:"
echo "kubectl port-forward service/resource-sizing-service 8080:8080 -n load-testing"
echo ""
echo "Health check: http://localhost:8080/actuator/health"
echo "Metrics: http://localhost:8080/actuator/prometheus"
echo "Employee API: http://localhost:8080/api/employees"
