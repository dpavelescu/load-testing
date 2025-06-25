@echo off
REM K6 Load Testing Runner Script for Windows
REM This script runs all K6 tests in sequence with proper setup and cleanup

setlocal enabledelayedexpansion

REM Configuration
set BASE_URL=%1
if "%BASE_URL%"=="" set BASE_URL=http://localhost:8080
set RESULTS_DIR=k6-results
set TIMESTAMP=%date:~-4,4%%date:~-10,2%%date:~-7,2%_%time:~0,2%%time:~3,2%%time:~6,2%
set TIMESTAMP=%TIMESTAMP: =0%

echo ========================================
echo K6 Load Testing Suite
echo ========================================
echo Base URL: %BASE_URL%
echo Timestamp: %TIMESTAMP%
echo Results Directory: %RESULTS_DIR%
echo ========================================

REM Create results directory
if not exist %RESULTS_DIR% mkdir %RESULTS_DIR%

REM Check if k6 is installed
k6 version >nul 2>&1
if errorlevel 1 (
    echo ERROR: k6 is not installed or not in PATH
    echo Please install k6 from https://k6.io/docs/getting-started/installation/
    pause
    exit /b 1
)

REM Check if service is running
echo Checking if service is available at %BASE_URL%...
curl -s -f %BASE_URL%/actuator/health >nul 2>&1
if errorlevel 1 (
    echo ERROR: Service is not available at %BASE_URL%
    echo Please ensure the Spring Boot service is running
    pause
    exit /b 1
)
echo Service is available!
echo.

REM Run Health Check Test
echo ========================================
echo Running Health Check Test...
echo ========================================
k6 run -e BASE_URL=%BASE_URL% --summary-export=%RESULTS_DIR%\health-check-%TIMESTAMP%.json k6-scripts\health-check-test.js
if errorlevel 1 (
    echo Health check failed! Stopping test suite.
    pause
    exit /b 1
)
echo Health check passed!
echo.

REM Wait between tests
echo Waiting 10 seconds before next test...
timeout /t 10 /nobreak >nul

REM Run Basic Load Test
echo ========================================
echo Running Basic Load Test...
echo ========================================
k6 run -e BASE_URL=%BASE_URL% --out json=%RESULTS_DIR%\basic-load-%TIMESTAMP%.json --summary-export=%RESULTS_DIR%\basic-load-summary-%TIMESTAMP%.json k6-scripts\basic-load-test.js
if errorlevel 1 (
    echo Basic load test failed!
    set BASIC_FAILED=1
) else (
    echo Basic load test completed!
)
echo.

REM Wait between tests
echo Waiting 15 seconds before memory test...
timeout /t 15 /nobreak >nul

REM Run Memory Simulation Test
echo ========================================
echo Running Memory Simulation Test...
echo ========================================
k6 run -e BASE_URL=%BASE_URL% --out json=%RESULTS_DIR%\memory-sim-%TIMESTAMP%.json --summary-export=%RESULTS_DIR%\memory-sim-summary-%TIMESTAMP%.json k6-scripts\memory-simulation-test.js
if errorlevel 1 (
    echo Memory simulation test failed!
    set MEMORY_FAILED=1
) else (
    echo Memory simulation test completed!
)
echo.

REM Summary
echo ========================================
echo Test Suite Summary
echo ========================================
echo Health Check: PASSED
if defined BASIC_FAILED (
    echo Basic Load Test: FAILED
) else (
    echo Basic Load Test: PASSED
)
if defined MEMORY_FAILED (
    echo Memory Simulation Test: FAILED
) else (
    echo Memory Simulation Test: PASSED
)
echo.
echo Results saved in: %RESULTS_DIR%
echo ========================================

REM Check if any tests failed
if defined BASIC_FAILED goto :failed
if defined MEMORY_FAILED goto :failed

echo All tests completed successfully!
pause
exit /b 0

:failed
echo Some tests failed. Check the results for details.
pause
exit /b 1
