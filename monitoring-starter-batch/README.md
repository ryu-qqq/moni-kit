# Monitoring Starter - Batch

> MoniKit의 `monitoring-starter-batch`는 Spring Batch 기반 애플리케이션에서  
> **배치 Job/Step 실행 로그 및 메트릭을 구조화된 형태로 수집**할 수 있도록 지원하는 경량 스타터입니다.

---

## 📦 주요 기능

- `JobExecutionListener`, `StepExecutionListener` 자동 등록
- 실행 시간, 상태, 스킵 수 등 상세 로그 수집
- `LogEntryContextManager` 기반 구조화 로깅
- `MetricCollector`, `LogSink`, `Hook` 기반 확장 가능
- `TraceIdProvider` 기반 trace 연동
- `monikit.logging.log-enabled=true` 설정 시 활성화

---

## ✅ 자동 등록되는 컴포넌트

| 컴포넌트 | 설명 |
|----------|------|
| `DefaultJobExecutionListener` | Job 실행 전후 로깅 및 메트릭 수집 |
| `DefaultStepExecutionListener` | Step 실행 후 상세 로그 수집 |

```java
@Bean
@ConditionalOnMissingBean(DefaultJobExecutionListener.class)
@ConditionalOnProperty(name = "monikit.logging.log-enabled", havingValue = "true")
public JobExecutionListener jobExecutionListener(...)

@Bean
@ConditionalOnMissingBean(DefaultStepExecutionListener.class)
@ConditionalOnProperty(name = "monikit.logging.log-enabled", havingValue = "true")
public StepExecutionListener stepExecutionListener(...)
```

---

## 🧾 로그 예시

```json
{
  "logType": "BATCH_STEP",
  "traceId": "abc-1234",
  "batchJobName": "productSyncJob",
  "stepName": "fetchProductsStep",
  "status": "COMPLETED",
  "executionTime": "582ms",
  "readCount": 100,
  "writeCount": 98,
  "skipCount": 2,
  "logLevel": "INFO"
}
```

---

## ⚙️ 설정 방법

`application.yml` 또는 `application.properties`에 다음과 같이 설정합니다:

```yaml
monikit:
  logging:
    log-enabled: true
```

| 설정 | 설명 | 기본값 |
|------|------|--------|
| `monikit.logging.log-enabled` | 배치 로깅 전반을 켜거나 끄는 마스터 스위치 | `false` |

---

## 🧩 확장 지점

| 대상 | 방법 |
|------|------|
| 로그 전송 방식 변경 | `LogSink` 구현체 등록 (예: `SlackSink`, `FileSink`) |
| 메트릭 수집 방식 변경 | `MetricCollector` 구현체 등록 (예: `PrometheusCollector`) |
| 로그 후처리 | `LogAddHook`, `LogFlushHook` 구현체 등록 |
| Trace ID 전략 변경 | `TraceIdProvider` 구현체 등록 |

---

## 🧪 테스트 유닛

- 모든 컴포넌트는 조건부 빈 등록 테스트 완료
- `log-enabled=false` 시 리스너 미등록 검증 포함
- `@Order(0)` 보장 → 사용자 리스너보다 우선 적용

---

## 📌 참고 모듈

- [`monitoring-core`](../monitoring-core)
- [`monitoring-config`](../monitoring-config)
- [`monitoring-starter`](../monitoring-starter)

---
