#!/bin/bash
# Simple monitoring access script - no passwords needed

echo "🚀 Simplified Monitoring Setup"
echo "=============================="

echo "1. Starting Prometheus port-forward..."
kubectl port-forward -n monitoring svc/prometheus 9090:9090 &
PROMETHEUS_PID=$!

echo "✅ Prometheus accessible at: http://localhost:9090"
echo ""
echo "📊 Key Queries for Resource Sizing:"
echo "   CPU Usage: rate(container_cpu_usage_seconds_total[1m])"
echo "   Memory Usage: container_memory_working_set_bytes"
echo "   JVM Memory: jvm_memory_used_bytes"
echo "   HTTP Requests: http_server_requests_total"
echo ""
echo "Press Ctrl+C to stop port-forwarding"

# Wait for interrupt
trap "echo 'Stopping...'; kill $PROMETHEUS_PID 2>/dev/null; exit 0" INT
wait
