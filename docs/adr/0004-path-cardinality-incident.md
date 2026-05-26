# ADR-0004: 장애 2 — HTTP path 카디널리티 폭증과 정규화 결정

- Status: Accepted
- Date: 2026-05-26
- Incident: HTTP path 가 그대로 메트릭 태그로 들어가 시계열 DB 가 수십만 시계열로 폭증
- Related: [ADR-0003](0003-timer-explosion-incident.md) (같은 패턴의 내부 트리거 버전)

## Context

`HttpResponseDurationMetricsBinder` 가 HTTP path 를 그대로 태그로 썼다.

```java
// 초기 구현
public void record(String path, int statusCode, long responseTime) {
    String key = path + "|" + statusCode;
    Timer timer = timerCache.computeIfAbsent(key, k ->
        Timer.builder("http_response_duration")
            .tag("path", path)
            .tag("status", String.valueOf(statusCode))
            .register(meterRegistry)
    );
    timer.record(responseTime, TimeUnit.MILLISECONDS);
}
```

문제: REST URL 에는 ID 같은 가변 segment 가 있다. `/api/users/1`, `/api/users/2`, `/api/users/3` ... 트래픽이 들어오는 만큼 unique path 태그가 늘어남.

[ADR-0003](0003-timer-explosion-incident.md) 의 장애 1과 본질적으로 같은 패턴인데, 트리거가 외부 URL 이라 훨씬 빠르게 도달. 100만 요청이면 unique userId 100만 개 → 시계열 100만 개.

**실제 운영 중 증상**

Prometheus 시계열 DB 입장에서 `http_response_duration{path="/api/users/1"}`, `{path="/api/users/2"}` 가 전부 별개 시계열. 수십만 시계열이 순식간에 생성. scrape 자체는 캡 (ADR-0003) 으로 막혔지만, 막힌 후로 새 endpoint 의 메트릭도 안 잡히는 부작용 발생.

## Decision

`normalizePath` 로 `\d+` 패턴을 `{id}` 로 치환.

```java
private String normalizePath(String path) {
    if (path == null) return "unknown";
    return path.replaceAll("\\d+", "{id}");
}
```

`/api/users/1` → `/api/users/{id}`, `/api/orders/42/items/7` → `/api/orders/{id}/items/{id}` 같이 정규화.

장애 1의 `MAX_TIMER_COUNT` 캡도 같이 걸려 있어서 최종 방어는 이중 — 정규화로 unique 수 자체를 줄이고, 캡으로 그래도 넘으면 거부.

## Consequences

### 좋은 점

- 숫자 ID 기반 REST endpoint 의 카디널리티 폭증을 즉시 차단
- 단순 정규식이라 비용 거의 없음 (path 당 한 번 `replaceAll`)
- 캡 (ADR-0003) 과 결합해서 이중 방어

### 나쁜 점

- 단순 `\d+` 치환이라 다음 패턴은 정규화 안 됨:
  - UUID: `/api/items/550e8400-e29b-41d4-a716-446655440000`
  - slug: `/posts/hello-world`, `/users/john-doe`
  - query param: `?userId=123` (path 가 아니라 query string)
  - mixed: `/api/v1/users/123` 에서 v1 도 숫자 포함이라 `/api/v{id}/users/{id}` 가 됨 (의도와 다름)
- 운영 환경별로 정규화 룰을 yml 등으로 외부화 안 됨 (하드코딩)
- 정규화 실패한 path 가 있어도 운영자가 모름

### 리스크

- UUID/slug 가 많은 앱에서는 이 정규화로 충분치 않음 → 결국 [ADR-0003](0003-timer-explosion-incident.md) 의 캡에 의존
- "v1" 같이 숫자 포함된 의도적 path segment 도 `{id}` 로 바뀌는 오탐 (low severity 지만 메트릭 의미가 깨짐)

### 대안

- **Spring Web `HandlerMethodMapping` path template** — 컨트롤러가 `@GetMapping("/api/users/{userId}")` 로 선언한 path template 을 그대로 가져와서 태그로 씀. 정규화의 진짜 정답. OTel Spring auto-instrumentation 이 이렇게 함.
- **Micrometer `WebMvcTagsProvider` / `WebMvcTagsContributor`** — Spring Boot Actuator 의 빌트인. URI 태그를 path template 기반으로 자동 생성.
- 외부화된 정규화 룰 yml — `/api/items/[uuid]/...` 같은 패턴을 사용자가 yml 로 선언

## 사전 차단 지표 (TODO)

지금은 정규화가 충분한지 알 방법이 없다. 추가해야 할 것:

- `monikit_path_normalization_ratio` — 정규화 적용된 path 비율 (낮으면 정규화 패턴이 부족하다는 시그널)
- `monikit_unique_path_count` — 정규화 후 unique path 수 (계속 늘면 정규화 룰 보강 필요)
- 캡 도달 시 어떤 path 들이 거부됐는지 샘플링 로그 (디버깅용)

## 관련

- README 의 [장애 2 섹션](../../README.md#장애-2--http-path-카디널리티-폭증)
- 코드: [HttpResponseDurationMetricsBinder.java](../../monitoring-metric/src/main/java/com/monikit/metric/HttpResponseDurationMetricsBinder.java#L49)
- 외부 참고: [Prometheus — Labels Best Practices](https://prometheus.io/docs/practices/naming/#labels), [Spring Boot — Actuator Web MVC Metrics](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.metrics.supported.spring-mvc)
