# Performance Analysis Instructions
## Time-Series Driven Analysis with Infrastructure Best Practices

### PRIMARY DIRECTIVE
**Execute time-series correlation analysis for EVERY performance test**, focusing on load vs impact relationships using infrastructure best practices—not isolated metrics or snapshots.

**CRITICAL RULE: NEVER interrupt running tests. All analysis is POST-TEST using time-series data.**

---

## STANDARDIZED ANALYSIS PROTOCOL

### PHASE 1: TEST EXECUTION (HANDS-OFF)
**During test execution:**
- ✅ **ONLY monitor Grafana dashboard** (http://localhost:3000)
- ❌ **NEVER run terminal commands** during test execution
- ✅ **Wait for "test finished" confirmation**

### PHASE 2: TIME-SERIES DATA COLLECTION (CONSISTENT)

#### 2.1 CORE TIME-SERIES QUERIES (5 queries only)
**Execute exactly these queries for every test:**

```bash
# 1. Request Load Timeline
curl -s "http://localhost:9191/api/v1/query_range?query=sum(rate(http_server_requests_seconds_count{job=\"resource-sizing-service\"}[1m]))&start=START_TIME&end=END_TIME&step=15s" > load-timeline.json

# 2. CPU Utilization Timeline  
curl -s "http://localhost:9191/api/v1/query_range?query=max(process_cpu_usage{job=\"resource-sizing-service\"})&start=START_TIME&end=END_TIME&step=15s" > cpu-timeline.json

# 3. Memory Pressure Timeline
curl -s "http://localhost:9191/api/v1/query_range?query=max(sum by (instance)(jvm_memory_used_bytes{job=\"resource-sizing-service\",area=\"heap\"}))&start=START_TIME&end=END_TIME&step=15s" > memory-timeline.json

# 4. Response Time Timeline
curl -s "http://localhost:9191/api/v1/query_range?query=max(rate(http_server_requests_seconds_sum{job=\"resource-sizing-service\"}[1m])/rate(http_server_requests_seconds_count{job=\"resource-sizing-service\"}[1m]))&start=START_TIME&end=END_TIME&step=15s" > latency-timeline.json

# 5. GC Impact Timeline
curl -s "http://localhost:9191/api/v1/query_range?query=max(rate(jvm_gc_pause_seconds_sum{job=\"resource-sizing-service\"}[1m]))&start=START_TIME&end=END_TIME&step=15s" > gc-timeline.json
```

### PHASE 3: CORRELATION ANALYSIS (INFRASTRUCTURE BEST PRACTICES)

#### 3.1 LOAD vs RESOURCE IMPACT CORRELATION
**Analyze these relationships from time-series data:**

1. **Load Scaling Efficiency**:
   - Does CPU usage scale linearly with request load?
   - Identify load thresholds where efficiency degrades
   - Detect resource saturation points

2. **Memory Pressure Correlation**:
   - How does memory usage correlate with request patterns?
   - Identify GC pressure points relative to load
   - Detect memory leak patterns over time

3. **Latency Degradation Analysis**:
   - At what load levels does latency increase non-linearly?
   - Correlate latency spikes with resource pressure
   - Identify performance cliff edges

#### 3.2 INFRASTRUCTURE PATTERN RECOGNITION
**Apply infrastructure best practices:**

1. **Resource Utilization Patterns**:
   - Baseline vs peak utilization ratios
   - Resource headroom during normal operations
   - Burst capacity requirements

2. **Scalability Indicators**:
   - Linear scaling boundaries
   - Resource bottleneck identification
   - Horizontal vs vertical scaling needs

3. **Stability Metrics**:
   - Performance consistency over time
   - Resource usage stability
   - Error rate correlation with load

### PHASE 4: SMART RECOMMENDATIONS (CORRELATED)

#### 4.1 RESOURCE SIZING (INFRASTRUCTURE BEST PRACTICES)
**Generate recommendations based on load correlation:**

**CPU Sizing Logic**:
```
IF peak_cpu < 50% AND load_scaling_linear: 
   RECOMMEND: Reduce CPU by (50% - peak_cpu)%, monitor performance
IF peak_cpu > 80% OR latency_degrades_with_load:
   RECOMMEND: Increase CPU by 25%, re-test scaling
IF 50% <= peak_cpu <= 80% AND good_scaling:
   RECOMMEND: CPU allocation optimal
```

**Memory Sizing Logic**:
```
IF gc_time < 2% AND memory_growth_stable:
   RECOMMEND: Memory allocation appropriate
IF gc_time > 5% OR memory_pressure_affects_latency:
   RECOMMEND: Increase heap by 30%, reduce GC pressure
IF memory_leaks_detected:
   RECOMMEND: Application investigation required
```

#### 4.2 SCALING RECOMMENDATIONS (CORRELATED ANALYSIS)
**Base decisions on load vs impact patterns:**

**Horizontal Scaling**:
- Trigger when: CPU/Memory usage scales linearly but approaches limits
- Pattern: Good load distribution, resource utilization consistent
- Action: Add replicas when baseline > 60% of limits

**Vertical Scaling**:
- Trigger when: Performance degrades before resource limits reached
- Pattern: Latency increases disproportionately to load
- Action: Increase pod resources, optimize application

**Application Optimization**:
- Trigger when: Resource usage disproportionate to load
- Pattern: High resource usage with low throughput
- Action: Profile application, optimize algorithms

#### 4.3 COST OPTIMIZATION (SMART CORRELATION)
**Correlate cost impact with performance needs:**

1. **Over-provisioning Detection**:
   - Consistent low utilization across load variations
   - No performance degradation at higher utilization
   - Clear headroom for reduction

2. **Right-sizing Calculations**:
   - Based on 95th percentile usage + 20% safety margin
   - Consider burst patterns and scaling events  
   - Factor in cost vs performance trade-offs

---

## EXECUTION CHECKLIST (SIMPLIFIED)

### ✅ PRE-TEST
- [ ] Grafana accessible (localhost:3000)  
- [ ] Prometheus accessible (localhost:9191)

### ✅ DURING TEST  
- [ ] Monitor Grafana only
- [ ] NO terminal commands
- [ ] Wait for completion

### ✅ POST-TEST (5 QUERIES + ANALYSIS)
- [ ] Collect 5 time-series datasets
- [ ] Analyze load vs resource correlations
- [ ] Generate correlated recommendations
- [ ] Apply infrastructure best practices

### ✅ DELIVERABLES
- [ ] Time-series correlation analysis
- [ ] Load-based resource recommendations  
- [ ] Infrastructure scaling guidance
- [ ] Cost optimization with performance context

---

## REPORT TEMPLATE (SIMPLIFIED)

```markdown
## PERFORMANCE ANALYSIS REPORT
**Test**: [NAME] | **Date**: [DATE] | **Duration**: [TIME]

### LOAD vs IMPACT CORRELATION
- **Load Pattern**: [LINEAR/STEPPED/BURST]
- **CPU Scaling**: [EFFICIENT/BOTTLENECK/OPTIMAL] 
- **Memory Pattern**: [STABLE/GROWING/PRESSURE]
- **Latency Behavior**: [CONSISTENT/DEGRADING/SPIKING]

### INFRASTRUCTURE ASSESSMENT
- **Resource Utilization**: [UNDER/OPTIMAL/OVER]
- **Scaling Readiness**: [HORIZONTAL/VERTICAL/OPTIMIZE]
- **Performance Stability**: [STABLE/VARIABLE/DEGRADING]

### CORRELATED RECOMMENDATIONS
1. **Immediate**: [ACTION] based on [CORRELATION]
2. **Scaling**: [STRATEGY] when [TRIGGER_CONDITION]  
3. **Cost**: [OPTIMIZATION] with [PERFORMANCE_IMPACT]
```



**Detect and classify:**
- **Latency spikes**: P99 > μ + 2.5σ
- **CPU anomalies**: Usage patterns outside IQR
- **Memory pressure**: Sustained growth without GC recovery
- **Load imbalances**: Pod RPS variance > 20%
- **GC pressure events**: Pause times > 95th percentile

### 4. PATTERN RECOGNITION & BEHAVIORAL ANALYSIS
**Extract performance regime characteristics:**
- **Linear scaling zone**: Resource-throughput proportionality (R² > 0.9)
- **Saturation onset**: First signs of performance degradation
- **Resource bottlenecks**: Limiting factors in scaling
- **Efficiency curves**: Optimal resource-to-performance ratios

### 5. PREDICTIVE MODELING
**Generate capacity planning insights:**

```bash
# Resource requirement projections
curl -s "http://localhost:9191/api/v1/query?query=predict_linear(process_cpu_usage{job=\"resource-sizing-service\"}[15m], 300)" > cpu-prediction.json
curl -s "http://localhost:9191/api/v1/query?query=predict_linear(jvm_memory_used_bytes{area=\"heap\",job=\"resource-sizing-service\"}[15m], 300)" > memory-prediction.json
```

**Calculate:**
- **Load scaling projections**: 2x, 5x, 10x current throughput
- **Resource saturation points**: Breaking point predictions
- **SLA compliance boundaries**: Maximum load for latency targets
- **Scaling recommendations**: Horizontal vs vertical optimization

---

## ANALYSIS DELIVERABLES GENERATION

### 1. Temporal Evolution Report
**Produce structured analysis:**
```
Performance Test Analysis: [Test ID] - [Timestamp]
═══════════════════════════════════════════════════════

LIFECYCLE PHASES DETECTED:
├── Baseline: [Time Range] - Statistical summary (μ ± σ)
├── Ramp-up: [Time Range] - Load gradient analysis
├── Steady State: [Time Range] - Performance stability metrics
├── Anomaly Events: [Time stamps] - Deviation analysis
└── Cool-down: [Time Range] - Recovery kinetics

PERFORMANCE REGIME ANALYSIS:
├── Linear scaling: [RPS range] - R² coefficient
├── Saturation onset: [Load threshold] - Degradation %
├── Resource efficiency: [RPS/resource ratios]
└── Optimal operating point: [Recommended load level]
```

### 2. Correlation Matrix & Statistical Analysis
```
CROSS-METRIC CORRELATIONS:
                RPS    CPU    Memory  P99Lat   GC
RPS           1.00   [r]     [r]     [r]    [r]
CPU           [r]    1.00    [r]     [r]    [r]
Memory        [r]    [r]     1.00    [r]    [r]
P99 Latency   [r]    [r]     [r]     1.00   [r]
GC Pressure   [r]    [r]     [r]     [r]    1.00

SIGNIFICANCE LEVELS: ** p<0.01, * p<0.05
```

### 3. Anomaly Detection Report
**Document all statistical deviations:**
- **Event timestamp and duration**
- **Affected metrics and severity (σ deviations)**
- **Root cause correlation analysis**
- **Impact on overall performance**
- **Recovery patterns and duration**

### 4. Predictive Insights & Recommendations
**Generate actionable optimization strategies:**

**RESOURCE OPTIMIZATION:**
- CPU allocation recommendations based on efficiency curves
- Memory sizing based on GC pressure patterns
- Pod scaling strategy (horizontal vs vertical)

**PERFORMANCE TUNING:**
- JVM/GC optimization based on memory patterns
- Connection pooling recommendations
- Cache optimization opportunities

**CAPACITY PLANNING:**
- Maximum sustainable load predictions
- Resource requirements for target loads
- SLA compliance boundaries
- Early warning threshold recommendations

---

## AUTOMATION INTELLIGENCE

### Real-Time Analysis Triggers
1. **Test completion detection**: Automatic analysis initiation
2. **Anomaly alerting**: Real-time deviation notifications
3. **Pattern comparison**: Historical performance regression detection
4. **Predictive warnings**: Resource saturation predictions

### Analysis Quality Assurance
- **Statistical confidence**: All correlations include significance tests
- **Temporal completeness**: Full lifecycle coverage verification
- **Cross-validation**: Multiple metrics confirm insights
- **Predictive accuracy**: Model validation against historical data

### Integration Protocol
1. **Prometheus data extraction**: Time-series queries for complete test duration
2. **Statistical analysis**: Correlation, regression, anomaly detection
3. **Pattern recognition**: Behavioral regime classification
4. **Report generation**: Structured markdown with actionable insights
5. **Historical archiving**: Performance baseline database maintenance

---

## SUCCESS CRITERIA CHECKLIST

### ✅ Analysis Completeness
- [ ] Complete test lifecycle automatically detected and characterized
- [ ] All major metrics included in correlation analysis
- [ ] Statistical significance testing applied to all insights
- [ ] Anomalies detected, classified, and root-cause analyzed

### ✅ Insight Quality
- [ ] Performance regimes identified with quantitative boundaries  
- [ ] Resource bottlenecks pinpointed through correlation analysis
- [ ] Predictive models generated with confidence intervals
- [ ] Recommendations backed by statistical evidence

### ✅ Actionability
- [ ] Specific resource allocation recommendations provided
- [ ] Scaling strategy determined based on bottleneck analysis
- [ ] Performance tuning opportunities quantified
- [ ] Monitoring thresholds recommended based on patterns

### ✅ Reproducibility
- [ ] Analysis methodology documented and reproducible
- [ ] All queries and calculations preserved for validation
- [ ] Historical comparison enables regression detection
- [ ] Pattern database updated for future analysis improvement

---

## EXAMPLE ANALYSIS OUTPUT TEMPLATE

```markdown
# Performance Test Analysis Report
**Test ID**: balanced-pressure-4rps-20240101-1400  
**Duration**: 25 minutes  
**Target Load**: 4.0 RPS sustained  
**Analysis Generated**: 2024-01-01 14:30:00 UTC

## Executive Summary
System demonstrated **linear scaling behavior** up to 3.8 RPS with **excellent load distribution** (CV=0.08). **Memory pressure** identified as primary bottleneck, with GC events causing periodic latency spikes. **Recommendation**: Increase heap allocation to 512MB for sustained 4+ RPS operation.

## Statistical Performance Profile
- **Baseline Performance**: CPU 5.2±1.1%, Memory 180±15MB, Latency 12±3ms
- **Steady-State Efficiency**: 2.1 RPS per 10% CPU, 0.8 RPS per 100MB memory
- **Linear Scaling Range**: 0-3.8 RPS (R²=0.96 CPU-throughput correlation)
- **Saturation Threshold**: 3.8 RPS (15% latency degradation onset)

## Anomaly Analysis
**Significant Events Detected:**
1. **GC Pressure Spike** @ 14:12:30 - P99 latency 450ms (3.2σ deviation)
   - **Cause**: Coordinated full GC across 2/3 pods
   - **Trigger**: Heap utilization > 85%
   - **Recovery**: 45 seconds to baseline

## Predictive Capacity Model
**Resource Requirements at Scale:**
- **2x Load (8 RPS)**: CPU 68%, Memory 340MB, P99 85ms
- **Breaking Point**: ~12 RPS (CPU saturation predicted)
- **SLA Compliance**: <100ms P99 sustainable up to 6 RPS

## Actionable Recommendations
**IMMEDIATE (Impact: High, Effort: Low)**
- Increase JVM heap to 512MB (eliminates GC pressure spikes)
- Set heap threshold alerts at 80% (early warning system)

**SCALING (Impact: Medium, Effort: Medium)**  
- Deploy 2nd pod at 6 RPS sustained load
- Implement G1GC with 256MB regions for allocation pattern

**MONITORING (Impact: High, Effort: Low)**
- Alert: Heap > 80%, Pod RPS variance > 15%, P95 latency > 75ms
- Dashboard: Add GC pressure correlation panel
```

**Execute this analysis protocol automatically after EVERY performance test to ensure consistent, intelligence-driven optimization.**

### 4. DASHBOARD LEGEND INTERPRETATION
**Understand pod-specific metrics using standardized legends:**

```
Pod Identification Standards:
┌─────────────────┬─────────────────────────────────┐
│ Metric Type     │ Legend Format                   │
├─────────────────┼─────────────────────────────────┤
│ Request Rate    │ Pod-{port} → "Pod-8080"        │
│ CPU Usage       │ Pod-{port} → "Pod-8080"        │
│ Heap Memory     │ Heap Pod-{port} → "Heap Pod-8080" │
│ NonHeap Memory  │ NonHeap Pod-{port} → "NonHeap Pod-8080" │
│ Live Threads    │ Live Pod-{port} → "Live Pod-8080" │
│ Peak Threads    │ Peak Pod-{port} → "Peak Pod-8080" │
│ GC Activity     │ {action}.{cause} Pod-{port}     │
└─────────────────┴─────────────────────────────────┘
```

**Cross-pod analysis patterns:**
```bash
# Analyze load distribution across pods
# Look for Pod-8080, Pod-8081, Pod-8082... series in Request Rate panel

# Resource utilization variance
# Compare CPU Usage between Pod-8080 vs Pod-8081 series

# Memory behavior correlation  
# Correlate Heap Pod-8080 with GC events from same pod
```
