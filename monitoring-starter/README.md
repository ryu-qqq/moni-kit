
# MoniKit Starter Monitoring

## 📌 개요

`monikit-starter-monitoring`은 **메서드 실행 시간**, **예외 발생 상황**, **메트릭 수집**을 자동으로 감지하고 기록하는 경량 AOP 기반 모니터링 스타터입니다.  
`@Service` 또는 `@Repository` 클래스에 선언된 메서드에 대해 실행시간과 예외를 자동으로 로깅하며, 커스터마이징 가능한 로그 수집 및 알림 확장 포인트를 제공합니다.

---

## ⚙️ 기본 기능

- 메서드 실행 시간 로깅 (ExecutionTimeLog)
- 파라미터 / 리턴 값 상세 로깅 (ExecutionDetailLog)
- 예외 발생 시 예외 로그 수집 및 분류 (ExceptionLog + ErrorCategory)
- MetricCollector를 통한 메트릭 연동
- 로그 컨텍스트 전파 및 MDC 연동
- 사용자 구현체 없을 시 기본 구현 자동 등록

---

## 🧩 주요 흐름

```text
@Service / @Repository 대상 메서드
        |
        ▼
[ExecutionLoggingAspect (AOP)]
        |
        ├─▶ ExecutionTimeLog / ExecutionDetailLog 기록
        ├─▶ ExceptionLog 기록 (예외 발생 시)
        └─▶ MetricCollector 연동
```

---

## 🧱 주요 구성 요소

### 1. `ExecutionLoggingAspect`
- `@Around` AOP로 메서드 실행 시간을 측정
- 입력/출력 값 로깅 (`detailed-logging = true`)
- 예외 발생 시 `ErrorCategory` 분류 후 예외 로그 기록

### 2. `ErrorCategoryClassifier`
- `SQLException`, `ConnectException`, `WebServerException` 등을 `ErrorCategory`로 분류
- 분류된 카테고리는 예외 로그에 포함됨

### 3. `LogEntryContextManagerConfig`
- `LogNotifier`, `ErrorLogNotifier`, `MetricCollector` 리스트 자동 주입
- 사용자 미등록 시 `DefaultLogEntryContextManager` 자동 사용
- 멀티스레드 로그 전파를 위한 `ThreadContextHandler`도 자동 등록

### 4. `LogNotifierAutoConfiguration`
- `LogNotifier` 기본 구현 (`DefaultLogNotifier`) 자동 주입

### 5. `ErrorLogNotifierAutoConfiguration`
- `ErrorLogNotifier` 기본 구현 (`DefaultErrorLogNotifier`) 자동 주입

### 6. `TraceIdProviderAutoConfiguration`
- `TraceIdProvider` 기본 구현 (`DefaultTraceIdProvider`) 자동 주입

---

## 📄 관련 설정 (application.yml)

```yaml
monikit:
  logging:
    log-enabled: true
    detailed-logging: true
```

---

## 📌 참고할 클래스

| 클래스명 | 설명 |
|----------|------|
| `ExecutionLoggingAspect` | 서비스/리포지토리 메서드 AOP 로깅 |
| `ErrorCategoryClassifier` | 예외를 ErrorCategory로 분류 |
| `LogEntryContextManagerConfig` | 핵심 구성 요소 자동 등록 |
| `LogNotifierAutoConfiguration` | 로그 알림 구현체 등록 |
| `ErrorLogNotifierAutoConfiguration` | 예외 알림 구현체 등록 |
| `TraceIdProviderAutoConfiguration` | TraceId 공급자 기본값 등록 |

---

## 🧪 테스트 팁

- `log-enabled=false`로 비활성화 테스트 가능
- `detailed-logging=true` 설정 시 input/output 로그 확인 가능
- 사용자 정의 구현체를 등록하면 기본 자동 설정은 무시됨
