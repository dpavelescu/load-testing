@echo off
REM Cleanup Kubernetes deployment (Windows)

echo 🧹 Cleaning up Resource Sizing Service deployment...

REM Delete all resources in reverse order
kubectl delete -f kubernetes/app/06-hpa.yaml
kubectl delete -f kubernetes/app/05-servicemonitor.yaml
kubectl delete -f kubernetes/app/04-service.yaml
kubectl delete -f kubernetes/app/03-deployment.yaml
kubectl delete -f kubernetes/app/02-configmap.yaml
kubectl delete -f kubernetes/app/01-namespace.yaml

echo ✅ Cleanup completed!

echo 📊 Remaining resources:
kubectl get all -n load-testing 2>nul || echo No resources found in load-testing namespace

pause
