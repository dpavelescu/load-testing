@echo off
echo ===============================================
echo Throughput-Based CPU Calibration Test
echo ===============================================

REM Set default values if not provided
if "%TARGET_RPS%"=="" set TARGET_RPS=3
if "%TEST_DURATION%"=="" set TEST_DURATION=10m
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8080

echo Target RPS: %TARGET_RPS%
echo Duration: %TEST_DURATION%  
echo Base URL: %BASE_URL%
echo.

REM Run the throughput test
k6 run -e TARGET_RPS=%TARGET_RPS% -e TEST_DURATION=%TEST_DURATION% -e BASE_URL=%BASE_URL% k6-scripts/throughput-cpu-calibration.js

echo.
echo ===============================================
echo Test completed. Check the results above.
echo For continuous monitoring, use:
echo   kubectl top pods -n load-testing --watch
echo ===============================================
pause
