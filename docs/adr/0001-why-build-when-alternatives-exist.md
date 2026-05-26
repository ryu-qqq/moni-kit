# ADR-0001: 왜 Micrometer/OTel 가 있는데 직접 짰는가

- Status: Accepted
- Date: 2026-05-26
- Related: [ADR-0002](0002-aop-vs-otel-agent.md)

## Context

Spring Boot 환경에서 "메서드별 실행 시간/병목을 자동으로 측정한다" 는 문제는 이미 잘 풀려 있다.

- Micrometer + Spring Boot Actuator → Prometheus / CloudWatch 메트릭 자동 노출
- OpenTelemetry Java Agent → 트레이스 + 로그 상관관계 + 메트릭 zero-code 수집
- Datadog / New Relic Agent → SaaS 풀스택

같은 문제에 검증된 도구가 셋이나 있는 상황에서 굳이 라이브러리를 직접 짠 이유와, 그 결과로 얻은 것을 기록한다.

## Decision

직접 짜기로 결정. 근거는 두 가지.

**(1) 단순한 호기심 — 자동 측정 + 동적 규칙을 한 yml 에서 다루고 싶었다**

영감은 우아한형제들 기술블로그의 [로그 및 SQL 진입점 정보 추가 여정](https://techblog.woowahan.com/13429/) (박지우, 2023). MDC 로 진입점을 추적하는 구조에서 한 발 더 나가 "AOP 로 메서드 실행 시간까지 묶으면?" 으로 확장.

기성품 도구들은 각각 잘 풀려 있지만, "어떤 메서드를 로깅할지 SpEL 규칙으로 yml 에서 동적 선언" 이라는 한 가지 자유도는 직접 짜야 얻을 수 있는 영역. 그 자유도 자체가 프로덕션 가치가 있는지는 별개 문제.

**(2) 학습 — 손으로 짜야 알 수 있는 함정이 있다**

기성품을 그냥 쓰면 "왜 이 API 가 이런 모양인지" 를 모른다. 손으로 짜야 그 설계의 이유가 코드로 보인다. 실제로 한 달간 만들면서 메트릭 운영의 함정 3건을 직접 부딪혔고 ([ADR-0003](0003-timer-explosion-incident.md), [ADR-0004](0004-path-cardinality-incident.md), [ADR-0005](0005-spel-evaluation-cost-incident.md)), 같은 함정이 기성품에서는 빌트인 API 로 이미 해결되어 있다는 것을 사후에 알게 됨.

이게 직접 짠 한 달의 진짜 산출물.

## Consequences

### 좋은 점

- AOP / SpEL / Micrometer / MDC 의 동작 원리를 코드 레벨로 이해함
- 메트릭 카디널리티 / Timer 폭증 / SpEL 평가 비용 같은 함정의 존재와 우회 패턴을 직접 학습
- "yml 만으로 로깅 대상을 동적 선언" 이라는 자유도는 실제로 구현됨

### 나쁜 점

- 같은 문제에 더 검증된 도구 (Micrometer + OTel Agent) 가 있어서 프로덕션 권장 불가
- 라이브러리 cover 영역이 좁음 — JDBC, HTTP client, Kafka 같은 third-party instrumentation 은 안 함
- 유지보수 비용을 본인이 떠안음

### 리스크

- 같은 문제를 풀려는 사람이 이 라이브러리를 프로덕션에 도입하면 운영 함정 (장애 1~3) 을 또 부딪힘. README 와 ADR 에 명시적으로 학습 목적이라고 적어 둠.

### 대안

- **Micrometer + Spring Boot Actuator** — 메트릭만 필요하면 정답
- **OpenTelemetry Java Agent** — 트레이스/로그/메트릭 묶어 자동 수집. cover 영역 압도적
- **Datadog/New Relic Agent** — SaaS 라 운영 비용 0. 비용은 결제로 해결

세 대안 모두 이 라이브러리보다 검증됐고 cover 영역이 넓다. 자유도 (동적 SpEL 규칙) 가 진짜 필요한 케이스에서만 직접 짠 라이브러리가 의미 있음.

## 관련

- README 의 [먼저 알아둘 것](../../README.md#️-먼저-알아둘-것-왜-만들었나) 섹션
- 한 달 회고: README [한 달 만들고 써본 회고](../../README.md#-한-달-만들고-써본-회고)
