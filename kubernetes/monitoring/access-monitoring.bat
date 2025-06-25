@echo off
REM Simple monitoring access script for Windows

echo 🚀 Simplified Monitoring Setup
echo ==============================

echo 1. Starting Prometheus port-forward...
start "Prometheus" cmd /k "kubectl port-forward -n monitoring svc/prometheus 9090:9090"

timeout /t 3 >nul

echo ✅ Prometheus accessible at: http://localhost:9090
echo.
echo 📊 Key Queries for Resource Sizing:
echo    CPU Usage: rate(container_cpu_usage_seconds_total[1m])
echo    Memory Usage: container_memory_working_set_bytes
echo    JVM Memory: jvm_memory_used_bytes
echo    HTTP Requests: http_server_requests_total
echo.
echo Press any key to open Prometheus in browser...
pause >nul
start http://localhost:9090
