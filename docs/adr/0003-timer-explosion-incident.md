# ADR-0003: 장애 1 — Micrometer Timer 무한 생성과 캡 결정

- Status: Accepted
- Date: 2026-05-26
- Incident: 운영 중 JVM 힙 우상향 + Prometheus scrape timeout + 시계열 DB 디스크 폭증
- Related: [ADR-0004](0004-path-cardinality-incident.md) (같은 패턴의 외부 트리거 버전)

## Context

처음 만든 메트릭 수집 코드는 단순했다. Binder 가 동적 태그로 `Timer` 를 만들고 `ConcurrentHashMap` 에 캐싱.

```java
// 초기 구현
public void record(String sql, String dataSource, long executionTime) {
    String key = sql + "|" + dataSource;
    Timer timer = timerMap.computeIfAbsent(key, k ->
        Timer.builder("sql_query_duration")
            .tag("query", sql)
            .tag("dataSource", dataSource)
            .register(meterRegistry)
    );
    timer.record(executionTime, TimeUnit.MILLISECONDS);
}
```

문제는 `sql` 이 동적으로 들어오는 경우.

- `?`-bind 가 안 된 native 쿼리 (`SELECT * FROM users WHERE id = 123`)
- 동적 `IN(...)` 쿼리 (`SELECT * FROM users WHERE id IN (1, 2, 3)`)
- ORM 이 생성한 쿼리에 리터럴 값이 박힘

매번 다른 sql 문자열 → 매번 다른 key → `ConcurrentHashMap` 무한 grow. 같은 구조의 Binder 가 셋 (`SqlQueryDurationMetricsBinder`, `HttpResponseDurationMetricsBinder`, `ExecutionDetailDurationMetricsBinder`) 이라 모두 동일 위험.

**실제 운영 중 증상**

- JVM 힙 사용량 우상향
- Prometheus scrape payload 가 수 MB 단위로 커지면서 scrape timeout
- 메트릭 시계열 DB 디스크 폭증
- 일부 인스턴스 OOM 직전까지 도달

## Decision

`Map` 크기 캡 (`MAX_TIMER_COUNT=100`) 을 두고, 캡 초과 시 새 키는 거부, 기존 키는 record 계속.

```java
private static final int MAX_TIMER_COUNT = 100;

public void record(String sql, String dataSource, long executionTime) {
    if (meterRegistry == null) return;
    String key = sql + "|" + dataSource;
    if (timerMap.size() >= MAX_TIMER_COUNT && !timerMap.containsKey(key)) {
        return;
    }
    Timer timer = timerMap.computeIfAbsent(key, k -> /* ... */);
    timer.record(executionTime, TimeUnit.MILLISECONDS);
}
```

3개 Binder 동일 패턴.

**왜 100인가?**

일반 Spring Boot 앱의 endpoint / SQL 종류 / AOP 대상 메서드 총합이 100 미만일 것이라는 경험적 추정. 진짜 답은 숫자 자체가 아니라 캡이 없으면 무한 grow 한다는 사실 자체를 인지하고 막는 것.

100 은 보수적인 시작점. 운영하면서 캡 도달이 잦으면 늘리고, 여유 있으면 줄이는 식으로 조정 필요한데 지금은 컴파일 타임 상수.

## Consequences

### 좋은 점

- 메모리 누수와 메트릭 폭증을 코드 레벨로 차단
- `computeIfAbsent` 의 race 도 안전 (`containsKey` 체크 후 거부 → 단순 size 비교 race 만 있고 그건 무해)
- 3개 Binder 동일 패턴이라 일관성

### 나쁜 점

- 100 은 임의 숫자. 환경별 동적 조정이 불가
- 캡 초과 시 **silently drop** — 운영자가 캡 도달을 모름
- 캡 도달 후에는 새로 등장한 SQL/path 의 메트릭이 영원히 안 잡힘 (jvm 재시작 전까지)

### 리스크

- 사전 차단 지표가 없어서 캡 도달도 모르고 진단도 어려움
- 100 보다 더 많은 endpoint 를 가진 앱에서는 캡이 바로 차서 의미 있는 메트릭을 못 잡을 수도 있음

### 대안

- **Micrometer `MeterFilter.maximumAllowableTags(...)`** — Micrometer 빌트인 API. 운영 가면 이걸로 가야 한다. 캡 도달 시 동작도 표준화되어 있고 metric 도 빠짐.
- **`MeterFilter.denyNameStartsWith(...)`** + cardinality 모니터링 — 패턴 기반으로 사전 차단
- 도구 자체 변경 — OTel Agent + Spring auto-instrumentation 이면 path template 기반이라 이 함정 자체가 발생 안 함

## 사전 차단 지표 (TODO)

지금은 캡 도달을 모른다. 추가해야 할 것:

- `monikit_timer_cache_size{binder="sql_query"}` 게이지 — Prometheus 알람 `> 80` 으로 사전 인지
- 캡 도달 시 WARN 로그 1회 출력 (반복 출력은 로그 폭증)
- 캡 도달 후 거부된 키의 카운터 (`monikit_timer_rejected_total`)

이게 있어야 "캡이 너무 작은지" 도 운영 데이터로 판단할 수 있음.

## 관련

- README 의 [장애 1 섹션](../../README.md#장애-1--micrometer-timer-무한-생성)
- 코드: [SqlQueryDurationMetricsBinder.java](../../monitoring-metric/src/main/java/com/monikit/metric/SqlQueryDurationMetricsBinder.java), [HttpResponseDurationMetricsBinder.java](../../monitoring-metric/src/main/java/com/monikit/metric/HttpResponseDurationMetricsBinder.java), [ExecutionDetailDurationMetricsBinder.java](../../monitoring-metric/src/main/java/com/monikit/metric/ExecutionDetailDurationMetricsBinder.java)
- 외부 참고: [Micrometer — Naming & Cardinality](https://micrometer.io/docs/concepts#_naming_meters)
