# MoniKit Metrics

Micrometer 기반 `MeterBinder` 와 `MetricCollector` 묶음.
MoniKit 의 구조화 로그 (`LogEntry`) 를 Prometheus 가 스크랩할 수 있는 메트릭으로 변환한다.

Spring Boot 환경에서는 `monitoring-starter` 가 자동으로 포함한다.

---

## 핵심 구성요소

| 컴포넌트 | 역할 |
|---|---|
| `MetricCollector<T>` | `LogEntry` 타입별 메트릭 수집 인터페이스 |
| `*MetricsBinder` | Micrometer `MeterRegistry` 연동 — `Timer` / `Counter` 등록 |
| `*MetricsRecorder` | 비즈니스 로직 기반 메트릭 기록 helper |
| `*MetricUtils` | 공통 처리 (path 정규화, 쿼리 정규화 등) |

---

## 메트릭 수집 흐름

```text
[LogEntry 생성]
    ↓
[MetricCollector.supports() 확인]
    ↓
[MetricCollector.record() 호출]
    ↓
[MetricsBinder 가 MeterRegistry 에 Timer/Counter 등록]
    ↓
[Prometheus /actuator/prometheus 엔드포인트로 노출]
```

---

## 등록되는 메트릭

| 이름 | 타입 | 태그 |
|---|---|---|
| `http_response_count` | Counter | `path`, `status` |
| `http_response_duration` | Timer | `path`, `status` |
| `sql_query_count` | Counter | `query_type`, `table` |
| `sql_query_duration` | Timer | `query`, `dataSource` |
| `execution_detail_count` | Counter | `class`, `method`, `tag` |
| `execution_duration` | Timer | `class`, `method`, `tag` |

Timer 는 모두 `publishPercentiles(0.5, 0.95, 0.99)` 설정.

---

## 가드 (장애 1/2 의 시스템 방어 위치)

3개 Binder 모두 다음 가드를 동일 패턴으로 구현하고 있다:

- `MAX_TIMER_COUNT = 100` — `ConcurrentHashMap` 캡. 새 키는 거부, 기존 키는 record 계속 → [ADR-0003](../docs/adr/0003-timer-explosion-incident.md)
- `normalizePath` — HTTP path `\d+` → `{id}` 치환 (`HttpResponseDurationMetricsBinder` 만) → [ADR-0004](../docs/adr/0004-path-cardinality-incident.md)

가드를 두게 된 운영 장애 맥락은 메인 [README](../README.md#-개발-후-운영하며-겪은-장애-3건) 와 위 ADR 에 자세히.

---

## 설정

```yaml
monikit:
  metrics:
    metrics-enabled: true          # 전체 메트릭 수집
    query-metrics-enabled: true    # SQL 메트릭
    http-metrics-enabled: true     # HTTP 메트릭
```

Spring Boot Actuator 의 Prometheus 엔드포인트 노출 설정도 필요:

```yaml
management:
  endpoints:
    web:
      exposure:
        include: prometheus, metrics
```

---

## 커스텀 MetricCollector 추가

```java
@Component
public class MyExecutionMetricCollector implements MetricCollector<ExecutionDetailLog> {

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.EXECUTION_DETAIL;
    }

    @Override
    public void record(ExecutionDetailLog logEntry) {
        Counter.builder("my_custom_count")
            .tag("service", logEntry.getClassName())
            .register(meterRegistry)
            .increment();
    }
}
```

`@ConditionalOnMissingBean` 으로 등록된 기본 구현체를 사용자가 직접 오버라이드 가능.

---

## 노출 확인

```bash
curl http://localhost:8080/actuator/prometheus | grep -E '(http_response|sql_query|execution_)'
```

---

## 미구현 / 한계

- `MAX_TIMER_COUNT` 도달을 알리는 사전 차단 지표 (`monikit_timer_cache_size` 같은 게이지) 가 아직 없음. 자세한 TODO 는 [ADR-0003](../docs/adr/0003-timer-explosion-incident.md#사전-차단-지표-todo).
- `normalizePath` 가 UUID/slug 는 처리 못 함. 자세한 한계는 [ADR-0004](../docs/adr/0004-path-cardinality-incident.md#consequences).
- 운영 환경에서는 이 모듈보다 [Micrometer `MeterFilter.maximumAllowableTags(...)`](https://micrometer.io/docs/concepts#_meter_filters) 같은 빌트인 API 가 더 안전한 선택.
