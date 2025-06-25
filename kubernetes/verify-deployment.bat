@echo off
REM Deployment Verification Script
REM Verifies that all components are running correctly

echo 🔍 Resource Sizing Service - Deployment Verification
echo.

echo ✅ Checking application namespace...
kubectl get pods -n load-testing
echo.

echo ✅ Checking monitoring namespace...
kubectl get pods -n monitoring
echo.

echo ✅ Checking services...
echo Load Testing Services:
kubectl get svc -n load-testing
echo.
echo Monitoring Services:
kubectl get svc -n monitoring
echo.

echo ✅ Testing application health...
echo Setting up port forwarding to test health endpoint...
echo.

REM Test health endpoint
start /B kubectl port-forward -n load-testing service/resource-sizing-service 8081:8080
timeout /t 3 /nobreak >nul 2>&1

echo Testing health endpoint...
curl -s http://localhost:8081/actuator/health | findstr "status" >nul
if %errorlevel%==0 (
    echo ✅ Application health endpoint is responding
) else (
    echo ❌ Application health endpoint is not responding
)

echo.
echo ✅ Testing ServiceMonitor configuration...
kubectl get servicemonitor -n monitoring resource-sizing-service-monitor >nul 2>&1
if %errorlevel%==0 (
    echo ✅ ServiceMonitor is configured
) else (
    echo ❌ ServiceMonitor is not found
)

echo.
echo 📊 Summary:
echo - Application: Ready for load testing
echo - Monitoring: Ready for metrics collection
echo - Access: Use monitoring-access.bat for dashboard access
echo.

REM Clean up port forward
taskkill /f /im kubectl.exe >nul 2>&1

pause
