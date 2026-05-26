# MoniKit

Spring Boot 환경에서 메서드별 실행 시간/병목을 AOP 로 자동 측정해보고 싶어서 한 달간 만든 학습 목적 라이브러리.
v1.1.4 의 핵심은 직접 운영하면서 겪은 장애 3건과 그 재발 방지 흔적이다.

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Micrometer](https://img.shields.io/badge/Micrometer-Prometheus-blue.svg)](https://micrometer.io/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Learning--Project-lightgrey.svg)](#-먼저-알아둘-것-왜-만들었나)

---

## ⚠️ 먼저 알아둘 것 (왜 만들었나)

원래 궁금증은 단순했다.

> "코드베이스에 `log.info(...)` 호출을 일일이 박는 대신, 어떤 지점에서 뭐가 얼마나 병목인지 자동으로 측정할 수는 없을까?"

직접적인 영감은 우아한형제들 기술블로그의 [로그 및 SQL 진입점 정보 추가 여정](https://techblog.woowahan.com/13429/) (박지우, 2023). MDC 로 어느 controller / event consumer / batch job 이 어떤 SQL 을 발생시켰는지 추적하는 구조를 다룬 글이다. 읽고 든 생각이 "그럼 한 발 더 나가서 AOP 로 메서드별 실행 시간까지 묶어서 자동화하면 어떨까?" 였고, 그렇게 시작한 게 이 라이브러리.

물론 같은 문제의 답은 이미 잘 풀려 있다.

| 도구 | 역할 |
|---|---|
| Micrometer + Spring Boot Actuator | Prometheus / CloudWatch 메트릭 자동 노출 |
| OpenTelemetry Java Agent | 트레이스 + 로그 상관관계 + 메트릭 zero-code 수집 |
| Datadog / New Relic Agent | SaaS 풀스택 관측성 |

프로덕션이라면 이 라이브러리는 쓸 필요 없다. 위 도구들이 더 검증됐고 cover 영역이 훨씬 넓다. 한 달 직접 만들고 운영해본 회고는 [맨 아래](#-한-달-만들고-써본-회고).

---

## ⚡ TL;DR

- 한 달간 개발 + 운영하며 실제 겪은 장애 3건과 재발 방지:
  1. Micrometer Timer 무한 생성 → `MAX_TIMER_COUNT=100` 캡으로 차단 ([코드](monitoring-metric/src/main/java/com/monikit/metric/SqlQueryDurationMetricsBinder.java#L24))
  2. HTTP path 카디널리티 폭증 → `normalizePath` 로 `\d+` → `{id}` 치환 ([코드](monitoring-metric/src/main/java/com/monikit/metric/HttpResponseDurationMetricsBinder.java#L49))
  3. DynamicMatcher SpEL 평가 비용 누적 → `thresholdMillis` 사전 필터 ([코드](monitoring-starter/src/main/java/com/monikit/starter/DynamicMatcher.java#L46))
- 5개 모듈 (`monitoring-core`, `-config`, `-starter`, `-starter-web`, `-metric`)
- AOP 기반 자동 로깅 + SpEL 동적 규칙 — `application.yml` 만으로 클래스/메서드/실행시간 패턴 매칭

---

## 📋 목차

- [먼저 알아둘 것](#️-먼저-알아둘-것-왜-만들었나)
- [TL;DR](#-tldr)
- [개발 후 운영하며 겪은 장애 3건](#-개발-후-운영하며-겪은-장애-3건)
- [결정 배경 Q&A](#-결정-배경-qa)
- [모듈 구조](#-모듈-구조)
- [빠른 시작](#️-빠른-시작)
- [환경](#-환경)
- [한 달 만들고 써본 회고](#-한-달-만들고-써본-회고)
- [ADR 목록](#-adr-목록)
- [참고 자료](#-참고-자료)

---

## 🛠 개발 후 운영하며 겪은 장애 3건

처음 만들었을 때 메트릭 수집 코드는 단순했다. SQL 쿼리마다 Timer, HTTP path 마다 Timer, 메서드마다 Timer. 첫 동작은 잘 됐다. 문제는 실제 트래픽이 다양한 호출 패턴을 흘려보낼 때부터.

각 장애에 대해 (1) 무슨 일이 났는가 → (2) 재발 방지를 위한 시스템 방어 → (3) 사전 차단을 위한 모니터링 지표 순으로 정리.

---

### 장애 1 — Micrometer Timer 무한 생성

**무슨 일이 났는가**

`SqlQueryDurationMetricsBinder` 가 SQL 쿼리별로 Timer 를 만들고 있었다. `?`-bind 가 안 된 native 쿼리, 동적 `IN(...)` 쿼리처럼 매번 다른 sql 문자열이 들어오면 매번 다른 key → `ConcurrentHashMap` 이 무한 grow. HTTP path / 메서드 시그니처 binder 도 같은 구조라 동일 위험.

운영 중에 JVM 힙 사용량이 우상향, Prometheus scrape payload 가 수 MB 단위로 커지면서 scrape timeout, 메트릭 시계열 DB 디스크 폭증.

**재발 방지를 위한 시스템 방어**

Map 크기 캡 (`MAX_TIMER_COUNT=100`) 을 두고, 캡 초과 시 새 키는 거부하고 기존 키만 record 계속.

```java
// SqlQueryDurationMetricsBinder.java
private static final int MAX_TIMER_COUNT = 100;

public void record(String sql, String dataSource, long executionTime) {
    if (meterRegistry == null) return;
    String key = sql + "|" + dataSource;
    if (timerMap.size() >= MAX_TIMER_COUNT && !timerMap.containsKey(key)) {
        return;
    }
    // ...
}
```

3개 Binder 동일 패턴 (`SqlQuery`, `HttpResponse`, `ExecutionDetail`).

**사전 차단을 위한 모니터링 지표**

진짜 방어는 캡을 두는 것보다 "캡에 가까워졌다" 를 사전에 알리는 것. 지금은 100 이 차면 silently drop 만 한다. 추가해야 할 것 (현재 미구현, TODO):

- `monikit_timer_cache_size{binder="sql_query"}` 같은 게이지 메트릭 → Prometheus 알람 `> 80` 으로 사전 인지
- 캡 도달 시 WARN 로그 1회 출력

`MAX_TIMER_COUNT=100` 도 임의 숫자다. 일반 Spring Boot 앱 endpoint/SQL/AOP 대상 총합이 100 미만일 거라는 경험적 추정 기반이고, 운영 환경별 동적 조정이 필요한데 지금은 컴파일 타임 상수.

자세한 결정 흐름: [ADR-0003](docs/adr/0003-timer-explosion-incident.md).

---

### 장애 2 — HTTP path 카디널리티 폭증

**무슨 일이 났는가**

`HttpResponseDurationMetricsBinder` 가 path 를 그대로 태그로 썼다. `/api/users/1`, `/api/users/2`, ... 트래픽이 들어오는 만큼 unique path 태그가 늘어남. 결과적으로 장애 1과 같은 양상인데, 트리거가 외부 URL 패턴이라 훨씬 빠르게 도달.

Prometheus 시계열 DB 입장에서 `http_response_duration{path="/api/users/1"}`, `{path="/api/users/2"}` 가 전부 별개 시계열. 수십만 시계열이 순식간에 생성.

**재발 방지를 위한 시스템 방어**

`normalizePath` 로 `\d+` 패턴을 `{id}` 로 치환.

```java
// HttpResponseDurationMetricsBinder.java
private String normalizePath(String path) {
    if (path == null) return "unknown";
    return path.replaceAll("\\d+", "{id}");
}
```

장애 1의 `MAX_TIMER_COUNT` 캡도 같이 걸려 있어서 최종 방어는 이중.

**사전 차단을 위한 모니터링 지표**

지금은 없다. 있으면 좋을 것 (TODO):

- `monikit_path_normalization_ratio` — 정규화 적용된 path 비율 (낮으면 정규화 패턴이 부족하다는 시그널)
- `monikit_unique_path_count` — 정규화 후 unique path 수 (계속 늘면 정규화 룰 보강 필요)

단순 `\d+` 정규식이라 UUID (`/api/items/550e8400-...`), slug (`/posts/hello-world`), query param 은 정규화 안 된다. 진짜 정답은 Spring Web 의 `HandlerMethodMapping` 기반 path template (`/api/users/{userId}`) 을 그대로 가져와서 태그로 쓰는 것 — OTel Spring auto-instrumentation 이 그렇게 한다.

자세한 결정 흐름: [ADR-0004](docs/adr/0004-path-cardinality-incident.md).

---

### 장애 3 — DynamicMatcher SpEL 평가 비용 누적

**무슨 일이 났는가**

`DynamicMatcher` 가 `when: "#executionTime > 100 && #args[0].size() > 50"` 같은 SpEL 표현을 모든 메서드 호출마다 평가했다. 1ms 도 안 걸리는 빠른 메서드 (getter, equals, 캐시 히트 메서드) 까지 동일하게 SpEL parse + evaluate → SpEL 자체 비용 (수백 ns ~ 수 us) 이 누적.

운영 중에 P99 응답 시간이 baseline 대비 수십 % 상승. profiling 으로 잡아보니 hot path 에 `SpelExpressionParser.parseExpression(...)` 이 올라옴.

**재발 방지를 위한 시스템 방어**

`DynamicLogRule` 에 `thresholdMillis` 필드 추가. SpEL 평가 전에 long 비교로 1차 거름.

```java
// DynamicMatcher.java
for (DynamicLogRule rule : rules) {
    if (!className.matches(rule.getClassNamePattern())) continue;
    if (!methodName.matches(rule.getMethodNamePattern())) continue;
    if (executionTime < rule.getThresholdMillis()) continue; // ← SpEL 평가 전 거름
    // SpEL 평가는 여기까지 통과한 것만
}
```

빠른 메서드는 SpEL 자체를 안 탄다.

**사전 차단을 위한 모니터링 지표**

지금은 없다. 있으면 좋을 것 (TODO):

- `monikit_spel_evaluation_count` — SpEL 호출 총 횟수
- `monikit_spel_evaluation_duration_seconds` — SpEL 평가 시간 분포 (P95/P99)

AOP 자체 오버헤드가 다른 메트릭에 묻히면 진단이 어려우니 self-metric 이 있어야 한다.

`thresholdMillis` 가 적용되어도 className/methodName regex 매칭은 매 호출마다 일어난다. 이것도 비용. 진짜 최적은 부팅 시 클래스 단위로 매칭 결과를 캐싱하는 것 — 지금은 미구현.

자세한 결정 흐름: [ADR-0005](docs/adr/0005-spel-evaluation-cost-incident.md).

---

## 🤔 결정 배경 Q&A

### Q1. 그냥 Micrometer + Actuator 쓰면 되잖아요?

맞다. 프로덕션이라면 Micrometer + Actuator + Prometheus + (선택) OTel Java Agent 가 정답.

이 라이브러리는 그 위에 "동적 SpEL 규칙으로 어떤 메서드를 로깅할지 yml 에서 선언" 이라는 한 가지 자유도를 추가할 뿐이다. 그 자유도가 진짜 필요한지는 케이스 by 케이스. [ADR-0001](docs/adr/0001-why-build-when-alternatives-exist.md).

### Q2. OpenTelemetry Java Agent 쓰면 자동인데 왜 AOP?

OTel Java Agent 가 더 넓다. JDBC, HTTP client, Kafka, gRPC, JVM runtime 등 cover 영역이 압도적이고 AOP 는 부분집합.

AOP 는 (1) Agent 부착이 불가한 환경 (2) SpEL 같은 도메인 규칙과 결합 시 손으로 짠 게 명시적이고 디버깅 쉬움 정도가 장점. 트레이드오프 매트릭스: [ADR-0002](docs/adr/0002-aop-vs-otel-agent.md).

### Q3. 장애 1의 `MAX_TIMER_COUNT=100`, 왜 하필 100인가?

100 자체는 임의 숫자다. 일반 Spring Boot 앱의 endpoint/SQL/AOP 대상 총합이 100 미만이라는 경험적 추정.

핵심은 숫자가 아니라 캡이 없으면 무한 grow 한다는 사실을 인지하고 캡을 두는 것 자체. 자세한 근거는 [ADR-0003](docs/adr/0003-timer-explosion-incident.md).

---

## 📦 모듈 구조

| 모듈 | 역할 |
|---|---|
| [`monitoring-core`](monitoring-core/README.md) | 순수 Java 기반 로깅/Hook 추상 + `TraceIdProvider` |
| [`monitoring-config`](monitoring-config/README.md) | `MoniKitLoggingProperties`, `DynamicLogRule` (SpEL 규칙 모델) |
| [`monitoring-starter`](monitoring-starter/README.md) | Spring Boot 자동 구성 + `ExecutionLoggingAspect` + `DynamicMatcher` |
| [`monitoring-starter-web`](monitoring-starter-web/README.md) | Web 전용 — `TraceIdFilter`, `LogContextScopeFilter` |
| [`monitoring-metric`](monitoring-metric/README.md) | Micrometer 기반 `MeterBinder` 모음 (장애 1~2 의 시스템 방어 위치) |

---

## ⚙️ 빠른 시작

### 의존성

Maven Central 미배포. [JitPack](https://jitpack.io/) 또는 `mavenLocal` 통해 빌드 후 사용.

```gradle
dependencies {
    implementation 'com.ryuqq:monikit-starter-web:1.1.4'
    implementation 'com.ryuqq:monikit-metric:1.1.4'
}
```

### application.yml

```yaml
monikit:
  logging:
    log-enabled: true
    allowed-packages:
      - com.example.service
    dynamic-matching:
      - classNamePattern: ".*Service"
        methodNamePattern: ".*"
        thresholdMillis: 100       # 장애 3 의 SpEL 사전 필터
        when: "#executionTime > 100"
        tag: "slow-service"
      - classNamePattern: ".*Repository"
        methodNamePattern: ".*"
        thresholdMillis: 500
        tag: "slow-query"
```

### 확인

```bash
# 애플리케이션 시작 로그
tail -f logs/application.log | grep "ExecutionLoggingAspect Registered"

# Prometheus 메트릭
curl http://localhost:8080/actuator/prometheus | grep -E '(http_response_duration|execution_duration|sql_query_duration)'
```

테스트는 각 모듈의 `src/test` 아래에 단위 테스트로 들어 있다 (`./gradlew test`).

---

## 🖥 환경

| 항목 | 값 |
|---|---|
| Java | 21+ |
| Spring Boot | 3.3.4 |
| Micrometer | Spring Boot 관리 의존성 |
| 빌드 도구 | Gradle 8.12.1 |
| 테스트 | JUnit 5, Mockito |

---

## 🪞 한 달 만들고 써본 회고

처음 호기심은 단순했다 — "메서드별 병목, 자동으로 측정할 수 없을까?". 한 달간 AOP / SpEL / Micrometer / MDC 를 손으로 다뤄보면서 "이게 왜 그렇게 설계됐는지" 를 코드로 학습한 건 분명히 값졌다.

한 달 만들고 운영해본 결론은, 그냥 Micrometer + Spring Boot Actuator + (선택) OpenTelemetry Java Agent 쓰는 게 답이다. 위 장애 3건이 그 결론의 근거다.

- 장애 1 (Timer 무한 생성) — Micrometer 의 [cardinality 가이드](https://micrometer.io/docs/concepts#_naming_meters) 와 `MeterFilter.maximumAllowableTags(...)` 가 같은 문제를 빌트인으로 풀어놓았다.
- 장애 2 (path 카디널리티 폭증) — Spring Web 의 `HandlerMethodMapping` 이 path template 으로 정확히 해결하고, OTel auto-instrumentation 이 그걸 그대로 태그로 쓴다.
- 장애 3 (SpEL 평가 비용) — OTel agent 의 [sampling](https://opentelemetry.io/docs/concepts/sampling/) + bytecode instrumentation 으로 hot path 자체에 코드가 안 박힌다.

내가 한 달 동안 부딪힌 함정은 이 분야 사람들이 이미 다 부딪혔고 도구 레벨에서 해결해놓은 것들이었다. 직접 짜보지 않았으면 이 함정들이 있는 줄도, 기성품이 그걸 어떻게 우회했는지도 몰랐을 거다. 그게 한 달 동안 얻은 것.

---

## 📐 ADR 목록

| # | 제목 |
|---|---|
| [0001](docs/adr/0001-why-build-when-alternatives-exist.md) | 왜 Micrometer/OTel 가 있는데 직접 짰는가 |
| [0002](docs/adr/0002-aop-vs-otel-agent.md) | AOP + SpEL vs OTel Java Agent 트레이드오프 |
| [0003](docs/adr/0003-timer-explosion-incident.md) | 장애 1 — Micrometer Timer 무한 생성과 캡 결정 |
| [0004](docs/adr/0004-path-cardinality-incident.md) | 장애 2 — HTTP path 카디널리티 폭증과 정규화 결정 |
| [0005](docs/adr/0005-spel-evaluation-cost-incident.md) | 장애 3 — SpEL 평가 비용 누적과 사전 필터 결정 |

---

## 📚 참고 자료

### 만들기 시작한 영감

- 우아한형제들 기술블로그 — [로그 및 SQL 진입점 정보 추가 여정](https://techblog.woowahan.com/13429/) (박지우, 2023-09-15)

### 의사결정 근거

- [Micrometer Concepts — Naming & Cardinality](https://micrometer.io/docs/concepts#_naming_meters)
- [Prometheus Best Practices — Labels](https://prometheus.io/docs/practices/naming/#labels)
- [Spring Boot Actuator Reference](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [OpenTelemetry Java Agent](https://opentelemetry.io/docs/zero-code/java/agent/)
- [OpenTelemetry Sampling](https://opentelemetry.io/docs/concepts/sampling/)
- [AspectJ Around Advice — 비용 분석](https://www.baeldung.com/spring-aop-vs-aspectj)

---

© 2024-2026 ryu-qqq. MoniKit.
