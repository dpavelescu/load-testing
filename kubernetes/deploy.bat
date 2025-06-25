@echo off
REM Deploy the application to Kubernetes (Windows)

echo 🚀 Deploying Resource Sizing Service to Kubernetes...

REM Check if kubectl is available
kubectl version --client >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ kubectl is not installed or not in PATH
    exit /b 1
)

REM Check if Docker Desktop Kubernetes is running
kubectl cluster-info >nul 2>&1
if %ERRORLEVEL% neq 0 (
    echo ❌ Kubernetes cluster is not accessible. Make sure Docker Desktop Kubernetes is enabled.
    exit /b 1
)

echo ✅ Kubernetes cluster is accessible

REM Build the Docker image
echo 🏗️ Building Docker image...
docker build -t resource-sizing-service:latest .
if %ERRORLEVEL% neq 0 (
    echo ❌ Docker build failed
    exit /b 1
)

echo ✅ Docker image built successfully

REM Apply Kubernetes manifests
echo 📦 Applying Kubernetes manifests...

REM Create namespace first
kubectl apply -f kubernetes/app/01-namespace.yaml

REM Apply all other manifests
kubectl apply -f kubernetes/app/02-configmap.yaml
kubectl apply -f kubernetes/app/03-deployment.yaml
kubectl apply -f kubernetes/app/04-service.yaml
kubectl apply -f kubernetes/app/05-servicemonitor.yaml
kubectl apply -f kubernetes/app/06-hpa.yaml

echo ✅ All manifests applied successfully

REM Wait for deployment to be ready
echo ⏳ Waiting for deployment to be ready...
kubectl wait --for=condition=available --timeout=300s deployment/resource-sizing-service -n load-testing

echo ✅ Deployment is ready!

REM Show status
echo 📊 Current status:
kubectl get pods -n load-testing
kubectl get services -n load-testing
kubectl get hpa -n load-testing

echo.
echo 🌐 To access the service:
echo kubectl port-forward service/resource-sizing-service 8080:8080 -n load-testing
echo.
echo Health check: http://localhost:8080/actuator/health
echo Metrics: http://localhost:8080/actuator/prometheus
echo Employee API: http://localhost:8080/api/employees

pause
