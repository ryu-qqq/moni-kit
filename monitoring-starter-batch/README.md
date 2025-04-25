# Monitoring Starter - Batch (v1.1.2)

> MoniKit의 `monitoring-starter-batch`는 Spring Batch 기반 애플리케이션에서
> **Job 및 Step의 실행 로그와 메트릭을 자동 수집**할 수 있도록 도와주는 경량 스타터입니다.

---

## 📦 주요 기능

- `JobExecutionListener`, `StepExecutionListener` 자동 등록
- `BatchJobLog`, `BatchStepLog` 기반 구조화 로그 수집
- 실행 시간, 상태, 처리량 등 핵심 메타 정보 자동 기록
- `LogEntryContextManager` 기반 로그 버퍼링 및 후처리 연동
- `MetricCollector` 기반 배치 메트릭 자동 수집 (성공/실패 카운트, duration 등)
- `TraceIdProvider` 기반 traceId 전파 지원

---

## ✅ 자동 등록 컴포넌트

| 컴포넌트 | 설명 |
|----------|------|
| `DefaultJobExecutionListener` | Job 실행 전후 로그 + 메트릭 수집 |
| `DefaultStepExecutionListener` | Step 실행 후 상세 로그 + 메트릭 수집 |
| `BatchJobMetricCollector` | `BatchJobLog`를 기반으로 메트릭 수집 |
| `BatchStepMetricCollector` | `BatchStepLog`를 기반으로 메트릭 수집 |
| `BatchJobMetricsRecorder` | Job 메트릭 수집을 실제로 수행하는 클래스 |
| `BatchStepMetricsRecorder` | Step 메트릭 수집 로직 담당 |

---

## 🧾 로그 포맷 예시

### Batch Step Log
```json
{
  "logType": "BATCH_STEP",
  "traceId": "abc-1234",
  "jobName": "productSyncJob",
  "stepName": "fetchProductsStep",
  "status": "COMPLETED",
  "executionTime": 582,
  "readCount": 100,
  "writeCount": 98,
  "skipCount": 2,
  "logLevel": "INFO"
}
```

---

## 📈 수집되는 메트릭 예시

| 메트릭 이름 | 설명 | 태그 |
|-------------|------|------|
| `batch_job_total` | Job 실행 수 | `job`, `status` |
| `batch_job_duration` | Job 실행 시간(ms) | `job` |
| `step_read_count` | Step read 수 | `job`, `step` |
| `step_write_count` | Step write 수 | `job`, `step` |
| `step_skip_count` | Step skip 수 | `job`, `step` |
| `step_duration` | Step 실행 시간(ms) | `job`, `step` |

---

## ⚙️ 설정 방법

```yaml
monikit.logging:
  log-enabled: true
  datasource-logging-enabled: true
  slow-query-threshold-ms: 1000
  critical-query-threshold-ms: 5000
  allowed-packages:
    - "com.ryuqq"
    - "com.monikit"
  dynamic-matching:
    - classNamePattern: "^External.*Client"
      methodNamePattern: ".*"
      when: "#executionTime > 1000"
      thresholdMillis: 1000
      tag: "external-api"
  metrics:
    metrics-enabled: true
    job-metrics-enabled: true
```

| 설정 키 | 설명 | 기본값 |
|----------|------|--------|
| `monikit.logging.log-enabled` | 로그 수집 활성화 여부 | `false` |
| `monikit.metrics.metrics-enabled` | 메트릭 전반 활성화 여부 | `true` |
| `monikit.metrics.job-metrics-enabled` | 배치 메트릭만 별도 제어 | `true` |

---

## 🔧 확장 포인트

| 대상 | 확장 방법 |
|-------|-----------|
| 로그 전송 방식 변경 | `LogSink` 구현체 등록 (예: `SlackSink`) |
| 메트릭 수집 방식 변경 | `MetricCollector<BatchJobLog>` 교체 등록 |
| Step 후처리 확장 | `LogAddHook`, `LogFlushHook` 활용 |
| TraceId 전략 교체 | `TraceIdProvider` 구현체 오버라이딩 |

---

## 🧪 테스트 보장

- 조건부 빈 등록 확인 (`log-enabled=false` 시 listener 미등록)
- `@Order(0)` 우선순위로 사용자 리스너보다 먼저 동작 보장
- Listener → Recorder → Collector → Binder 흐름 단위 테스트 완료

---

## 🔗 연관 모듈

- [`monitoring-core`](../monitoring-core)
- [`monitoring-config`](../monitoring-config)
- [`monitoring-metric`](../monitoring-metric)
- [`monitoring-starter`](../monitoring-starter)

---

> 이 스타터는 Spring Batch 환경에서의 실행 흐름을 구조화하고, 운영과 관측을 일관되게 유지하기 위한 핵심 구성 요소입니다.


<details>
<summary><strong>Grafana 대시보드 쿼리 예시 (for monitoring-starter-batch)</strong></summary>

# 📊 Grafana 대시보드 쿼리 예시 (for monitoring-starter-batch)
> 이 문서는 `monitoring-starter-batch`에서 자동 수집되는 Prometheus 메트릭을 기반으로
> Grafana에서 시각화할 수 있는 쿼리 예시를 제공합니다.

---

## ✅ 배치 Job 메트릭

### 1. Job 실행 횟수 (성공/실패)
```promql
sum by (job, status) (batch_job_total)
```

### 2. Job 평균 실행 시간 (ms)
```promql
avg by (job) (rate(batch_job_duration_sum[5m]) / rate(batch_job_duration_count[5m]))
```

### 3. Job 성공률 (%)
```promql
sum by (job) (batch_job_total{status="success"})
/ ignoring(status) 
(sum by (job) (batch_job_total)) * 100
```

---

## 🔍 배치 Step 메트릭

### 4. Step별 처리량 (write 기준)
```promql
sum by (job, step) (step_write_count)
```

### 5. Step 평균 실행 시간
```promql
avg by (step) (rate(step_duration_sum[5m]) / rate(step_duration_count[5m]))
```

### 6. Step 누적 스킵 수
```promql
sum by (job, step) (step_skip_count)
```

### 7. Step duration 상위 5개 (가장 오래 걸리는 Step)
```promql
topk(5, avg by (step) (rate(step_duration_sum[5m]) / rate(step_duration_count[5m])))
```

---

## 📁 참고 메트릭 명세

| 메트릭 이름 | 설명 | 태그 |
|-------------|------|------|
| `batch_job_total` | Job 실행 횟수 | `job`, `status` |
| `batch_job_duration` | Job 실행 시간(ms) | `job` |
| `step_read_count` | Step read 수 | `job`, `step` |
| `step_write_count` | Step write 수 | `job`, `step` |
| `step_skip_count` | Step skip 수 | `job`, `step` |
| `step_duration` | Step 실행 시간(ms) | `job`, `step` |

---

> 이 쿼리들을 통해 운영 중인 배치의 성능, 안정성, 병목 구간 등을 효과적으로 모니터링할 수 있습니다.
> Prometheus + Grafana 환경에서 대시보드에 직접 적용해보세요.

</details>