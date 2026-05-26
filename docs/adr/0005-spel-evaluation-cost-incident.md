# ADR-0005: 장애 3 — SpEL 평가 비용 누적과 사전 필터 결정

- Status: Accepted
- Date: 2026-05-26
- Incident: 운영 중 P99 응답 시간이 baseline 대비 수십 % 상승. profiling 으로 hot path 에 SpEL 평가가 잡힘
- Related: [ADR-0002](0002-aop-vs-otel-agent.md) (AOP 채택의 비용이 어떻게 나타나는지)

## Context

`DynamicMatcher` 는 yml 의 `dynamic-matching` 규칙을 모든 메서드 호출에 대해 평가한다.

```yaml
monikit:
  logging:
    dynamic-matching:
      - classNamePattern: ".*Service"
        methodNamePattern: ".*"
        when: "#executionTime > 100 && #args[0].size() > 50"
        tag: "slow-service"
```

초기 구현은 단순했다.

```java
// 초기 구현
for (DynamicLogRule rule : rules) {
    if (!className.matches(rule.getClassNamePattern())) continue;
    if (!methodName.matches(rule.getMethodNamePattern())) continue;

    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("executionTime", executionTime);
    context.setVariable("args", args);

    Boolean result = parser.parseExpression(rule.getWhen()).getValue(context, Boolean.class);
    if (Boolean.TRUE.equals(result)) return Optional.of(rule);
}
```

문제: AOP 가 cover 하는 모든 메서드 (Spring Bean 의 거의 모든 메서드) 가 매 호출마다 SpEL parse + evaluate 를 탄다. 1ms 도 안 걸리는 빠른 메서드 — getter, equals, 캐시 히트 가능한 메서드, 단순 위임 메서드 — 도 동일.

SpEL 자체 비용은 수백 ns ~ 수 us 인데, 호출 빈도가 높은 hot path 에서는 이게 누적되어 baseline 의 수십 % 까지 차지.

**실제 운영 중 증상**

- P99 응답 시간이 baseline 대비 수십 % 상승
- profiling 으로 hot path 에 `SpelExpressionParser.parseExpression(...)` 이 올라옴
- AOP 자체 오버헤드보다 SpEL 평가 비용이 더 큰 경우 발견

## Decision

`DynamicLogRule` 에 `thresholdMillis` 필드 추가. SpEL 평가 전에 빠른 `long` 비교로 1차 거름.

```java
// DynamicMatcher.java (현재)
for (DynamicLogRule rule : rules) {
    if (!className.matches(rule.getClassNamePattern())) continue;
    if (!methodName.matches(rule.getMethodNamePattern())) continue;
    if (executionTime < rule.getThresholdMillis()) continue; // ← 1차 거름

    if (rule.getWhen() == null || rule.getWhen().isBlank()) return Optional.of(rule);

    StandardEvaluationContext context = new StandardEvaluationContext();
    context.setVariable("executionTime", executionTime);
    context.setVariable("className", className);
    context.setVariable("methodName", methodName);
    context.setVariable("args", args);

    Boolean result = parser.parseExpression(rule.getWhen()).getValue(context, Boolean.class);
    if (Boolean.TRUE.equals(result)) return Optional.of(rule);
}
```

`thresholdMillis` 가 1차 거름 역할. 빠른 메서드는 SpEL 자체를 안 탄다.

yml 사용 예:

```yaml
dynamic-matching:
  - classNamePattern: ".*Service"
    methodNamePattern: ".*"
    thresholdMillis: 100        # 100ms 미만은 SpEL 평가도 안 함
    when: "#executionTime > 100"
    tag: "slow-service"
```

## Consequences

### 좋은 점

- 빠른 메서드 (대부분) 가 SpEL 평가를 건너뛰면서 hot path 비용 절감
- `long` 비교라 비용 거의 0
- yml 에서 사용자가 명시적으로 threshold 를 지정 — "어떤 메서드가 hot path 인지" 의 의도가 코드로 보임

### 나쁜 점

- `className.matches(...)` / `methodName.matches(...)` 의 regex 매칭은 여전히 매 호출마다 일어남 (이것도 비용)
- 클래스 단위로 매칭 결과를 캐싱하면 더 줄일 수 있지만 미구현
- `thresholdMillis` 를 0 으로 두면 다시 모든 호출이 SpEL 을 탐 — 사용자가 실수하면 효과 없음
- AOP 자체 (around advice) 의 오버헤드는 그대로

### 리스크

- 빠른 메서드 중 진짜 로깅이 필요한 케이스 (예: 1ms 안에 끝나야 하는데 가끔 10ms 걸리는 케이스) 를 `thresholdMillis: 5` 같이 잡으면 다시 SpEL 평가량이 많아짐
- SpEL self-metric 이 없어서 운영 중 SpEL 평가 비용이 얼마나 발생하는지 모름

### 대안

- **OTel Java Agent의 sampling + bytecode instrumentation** — hot path 자체에 코드가 안 박혀서 평가 비용 자체가 없음. 일정 비율만 trace 수집
- **부팅 시 클래스 단위 매칭 캐싱** — `Map<Class<?>, List<DynamicLogRule>>` 로 클래스별 매칭 룰을 미리 결정. AOP advice 에서는 lookup 만
- **annotation 기반** (`@Timed` 등) — Micrometer 의 `@Timed`. 동적 SpEL 자유도는 없지만 비용 0
- **컴파일 타임 코드 생성** — APT/KAPT 같은 annotation processor 로 빌드 시점에 instrumentation 코드 생성

## 사전 차단 지표 (TODO)

지금은 SpEL 평가 비용이 얼마나 들고 있는지 self-metric 이 없다. 추가해야 할 것:

- `monikit_spel_evaluation_count` — SpEL 호출 총 횟수
- `monikit_spel_evaluation_duration_seconds` — SpEL 평가 시간 분포 (P95/P99)
- `monikit_dynamic_matcher_prefilter_skip_total` — pre-filter 로 SpEL 평가를 건너뛴 횟수 (효과 검증용)

AOP 자체 오버헤드가 다른 비즈니스 메트릭에 묻히면 진단이 어려우니 self-metric 이 있어야 한다.

## 관련

- README 의 [장애 3 섹션](../../README.md#장애-3--dynamicmatcher-spel-평가-비용-누적)
- 코드: [DynamicMatcher.java](../../monitoring-starter/src/main/java/com/monikit/starter/DynamicMatcher.java#L43)
- 외부 참고: [OpenTelemetry Sampling](https://opentelemetry.io/docs/concepts/sampling/), [Spring AOP vs AspectJ — 비용 분석](https://www.baeldung.com/spring-aop-vs-aspectj)
