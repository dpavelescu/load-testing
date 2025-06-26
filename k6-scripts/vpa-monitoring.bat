@echo off
echo ===========================================
echo VPA Sustained Monitoring Script
echo Starting 2-hour monitoring session...
echo ===========================================

set START_TIME=%TIME%
echo Start Time: %START_TIME%

for /L %%i in (1,1,8) do (
    echo.
    echo ===========================================
    echo Monitoring Cycle %%i of 8 ^(every 15 minutes^)
    echo Current Time: %TIME%
    echo ===========================================
    
    REM Run a quick load test
    echo Running load test...
    k6 run basic-load-test.js
    
    REM Check resource usage
    echo.
    echo Checking resource usage...
    kubectl top pod -n load-testing
    
    REM Check VPA status every other cycle
    set /A "cycle=%%i %% 2"
    if !cycle! == 0 (
        echo.
        echo Checking VPA status...
        kubectl get vpa resource-sizing-service-vpa -n load-testing -o yaml
        echo.
        echo Checking VPA logs...
        kubectl logs -n kube-system -l app=vpa-recommender --tail=5
    )
    
    REM Wait 15 minutes (900 seconds) unless it's the last cycle
    if %%i LSS 8 (
        echo.
        echo Waiting 15 minutes until next cycle...
        echo Press Ctrl+C to stop monitoring
        timeout /t 900 /nobreak > nul
    )
)

echo.
echo ===========================================
echo Monitoring Complete!
echo Start Time: %START_TIME%
echo End Time: %TIME%
echo ===========================================
echo.
echo Final VPA Check:
kubectl get vpa resource-sizing-service-vpa -n load-testing -o yaml
echo.
echo Final Resource Usage:
kubectl top pod -n load-testing
