# MoniKit Starter

## 개요

`monikit-starter`는 MoniKit Core의 구성 요소들을 Spring Boot 기반 프로젝트에 자동으로 통합해주는 모듈입니다.
이 스타터는 `monikit-core`, `monikit-config`, `monikit-metric`을 내부적으로 포함하며,
사용자는 이 모듈 하나만 의존해도 다음 기능을 사용할 수 있습니다:

- 로그 수집 및 추적 ID (traceId) 기반 AOP 로깅
- 동적 로깅 규칙 기반 메서드 실행 시간 기록
- Micrometer 기반의 HTTP/SQL 메트릭 수집 및 노출
- MDC 기반 컨텍스트 전파 및 로그 연동

---

## 자동 구성 기능 요약

### Core / Logging

- `ExecutionLoggingAutoConfiguration`: AOP 기반 실행 시간 기록
- `LogEntryContextManagerConfig`: 로그 수집 컨텍스트 등록
- `TraceIdProviderAutoConfiguration`: Trace ID 제공자 자동 등록 (MDC 기반)

### Metrics (Micrometer)

- `MoniKitMeterBinderAutoConfiguration`: HTTP/SQL 전용 `MeterBinder` 자동 등록
- `MetricCollectorHookAutoConfiguration`: 로그 수집 시 메트릭도 함께 기록
    - 조건: `monikit.metrics.metrics-enabled=true`

### Configuration Properties 바인딩

- `MoniKitLoggingPropertiesAutoConfiguration`
- `MoniKitMetricsPropertiesAutoConfiguration`

---

## 포함된 구조

```
monitoring-starter
├── monitoring-core      # 핵심 인터페이스, LogEntry, Hook 시스템
├── monitoring-config    # SpEL 규칙, 동적 매칭 설정
└── monitoring-metric    # Micrometer 기반 메트릭 수집
```

---

## 🔧 설정 예시 (application.yml)

```yaml
monikit.logging:
  log-enabled: true
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
```

---

## 💡 확장성

- `LogSink`, `MetricCollector`, `QueryLoggingService` 등은 모두 **@ConditionalOnMissingBean** 으로 정의되어 있어,
  사용자가 직접 구현체를 등록하면 자동으로 오버라이드됩니다.
- 설정값만으로 모든 기능이 동작하며, 코드 변경 없이 슬로우 쿼리, HTTP 메트릭 수집 등을 시작할 수 있습니다.

---

## 🧪 테스트 팁

- 설정 비활성화 시 기능 제거 테스트 가능
- `monikit.logging.log-enabled=false` → 전체 로깅 차단
- `monikit.metrics.metrics-enabled=false` → 메트릭 미수집

---

(c) 2025 Ryu Sangwon. MoniKit 프로젝트
