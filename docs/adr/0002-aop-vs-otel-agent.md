# ADR-0002: AOP + SpEL vs OTel Java Agent 트레이드오프

- Status: Accepted
- Date: 2026-05-26
- Related: [ADR-0001](0001-why-build-when-alternatives-exist.md), [ADR-0005](0005-spel-evaluation-cost-incident.md)

## Context

"메서드별 실행 시간 자동 측정" 을 구현하는 방법은 크게 두 가지.

- **AOP (AspectJ Around advice)** — Spring AOP 로 메서드 진입/종료 hook
- **OpenTelemetry Java Agent** — bytecode instrumentation 으로 JVM 시작 시 클래스를 재작성

기성품 (OTel Agent) 가 cover 영역도 넓고 검증도 잘 되어 있는데, 이 라이브러리는 AOP 를 채택했다. 그 이유와 비용을 기록한다.

## Decision

AOP + SpEL 채택. 트레이드오프 매트릭스는 다음.

| 항목 | AOP + SpEL (MoniKit) | OTel Java Agent |
|---|---|---|
| 부착 방식 | 의존성 추가 + Spring Boot 자동 구성 | JVM `-javaagent` 옵션 (배포 환경 변경) |
| Cover 영역 | Spring Bean 의 메서드만 (AOP 대상) | JDBC, HTTP client, Kafka, gRPC, JVM runtime 등 |
| 도메인 규칙 결합 | yml + SpEL 로 명시적 선언 가능 | annotation 또는 별도 설정 필요 |
| 코드 위치 | hot path 에 AOP 코드 박힘 | hot path 에 코드 없음 (bytecode 재작성) |
| 오버헤드 | SpEL 평가 비용 (장애 3 참고) | bytecode 수준 → 거의 무시 가능 |
| 디버깅 가시성 | 코드로 보임 | bytecode 라 IDE 추적 어려움 |
| Agent 부착 불가 환경 | OK (의존성만 있으면 됨) | 불가 (예: 일부 서버리스 cold start 민감) |
| 검증 수준 | 본인 만든 라이브러리 (낮음) | 업계 표준, 광범위 검증 |

AOP 가 더 나은 영역은 (1) 도메인 규칙과 결합 (yml 의 SpEL `when:` 으로 "어떤 메서드를 로깅할지" 동적 선언) (2) Agent 부착이 불가한 환경 — 둘 다 좁은 영역.

이 라이브러리가 AOP 를 쓰는 진짜 이유는 (1) 학습 목적 (2) 도메인 규칙 결합이라는 한 가지 자유도 — 그 외는 OTel Agent 가 모든 면에서 더 낫다.

## Consequences

### 좋은 점

- yml + SpEL 만으로 로깅 대상을 동적 선언 — 코드 변경 없이 운영 중 규칙 조정 가능
- AOP 코드가 명시적이라 디버깅/리뷰가 쉬움
- Agent 부착 불가 환경에서도 동작

### 나쁜 점

- Cover 영역이 좁음 — third-party 라이브러리 (JDBC, Kafka, HTTP client) 의 instrumentation 은 직접 짜야 함
- hot path 에 SpEL 평가가 들어가서 빠른 메서드에서 오버헤드 누적 ([ADR-0005](0005-spel-evaluation-cost-incident.md) 의 장애 3 가 이 원인)
- OTel Agent 대비 검증 수준이 압도적으로 낮음

### 리스크

- "SpEL 자유도가 정말 필요한 케이스" 가 아니면 AOP 의 비용만 떠안고 cover 영역의 손해는 그대로
- 본인 라이브러리라 OTel Agent 처럼 광범위 커뮤니티 검증이 없음

### 대안

- **OTel Java Agent** — 자동 instrumentation, cover 영역 압도적. 대부분의 케이스에서 정답
- **Spring AOP annotation 기반** (`@Timed` 등) — Micrometer 의 `@Timed` 가 이미 있음. 동적 SpEL 자유도는 없음
- **Datadog APM Agent** — SaaS, 운영 비용 0

## 관련

- README 의 [결정 배경 Q&A — Q2](../../README.md#q2-opentelemetry-java-agent-쓰면-자동인데-왜-aop)
- [ADR-0005](0005-spel-evaluation-cost-incident.md) — AOP 채택의 비용이 실제로 어떻게 나타났는지
