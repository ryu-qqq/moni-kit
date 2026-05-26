# MoniKit Core

## 개요

`monikit-core` 는 구조화 로그 (`LogEntry`), 컨텍스트 전파 (ThreadLocal), Hook, MetricCollector 추상화를 담는 모듈.
Spring 같은 외부 프레임워크 의존성 없는 순수 Java 코드만 들어 있어서 어디서든 import 해 쓸 수 있다.

Spring 환경에서는 `monitoring-starter` 가 이걸 가져다 자동 구성한다.

---

## 설계 원칙

- 외부 프레임워크 의존성 0 (순수 Java)
- 컨텍스트 전파 / 로그 수집 / 메트릭 기록 / 전송 전략을 도메인 단위로 분리
- 실행 주체 (Runnable, Thread 등) 는 core 에 포함하지 않음 — starter 모듈이 담당

---

## 디렉토리 구성

| 패키지 | 책임 |
|--------|------|
| `model/` | 구조화 로그 엔트리 (`LogEntry` 구현체) 정의 |
| `context/` | 로그 수집 컨텍스트 관리 (`LogEntryContext`, `ContextManager`) |
| `concurrent/` | ThreadLocal 기반 컨텍스트 전파 처리기 (`ThreadContextHandler`) |
| `hook/` | 로그 수집 시점/flush 시점의 후처리 Hook 정의 및 확장 커스터마이저 |
| `notifier/` | 로그 전송 오케스트레이터 (`LogNotifier`, `LogSink`) 및 Sink 확장 |

---

## 주요 클래스 요약

### 🔹 LogEntry 인터페이스
```java
public interface LogEntry {
    Instant getTimestamp();
    String getTraceId();
    LogType getLogType();
    LogLevel getLogLevel();
    String toString();
}
```
- 모든 구조화 로그의 공통 구조
- JSON 직렬화를 통해 로그 시스템 전송에 최적화

---

### 🔹 LogNotifier & LogSink
```java
public interface LogNotifier {
    void notify(LogLevel logLevel, String message);
    void notify(LogEntry logEntry);
}

public interface LogSink {
    boolean supports(LogType logType);
    void send(LogEntry logEntry);
}
```
- `LogNotifier`는 여러 개의 `LogSink`를 가지고 타입 기준으로 분배
- `LogSink`는 전송 전략(예: Slack, File, Console 등)
- `LogSinkCustomizer`를 통해 확장 가능

---

### 🔹 LogEntryContextManager
```java
public interface LogEntryContextManager {
    void addLog(LogEntry logEntry);
    void flush();
    void clear();
}
```
- 요청 단위 로그 저장 및 후처리 전송 담당
- 내부적으로 Hook, Notifier 연동

---

### 🔹 Hook 구조 (onAdd / onFlush)
- `LogAddHook`: 로그 추가 시점 후처리
- `LogFlushHook`: flush 시점 전체 로그 후처리
- 각각 `HookCustomizer`로 동적 확장 가능

---

### 🔹 MetricCollector
```java
public interface MetricCollector<T extends LogEntry> {
    boolean supports(LogType type);
    void record(T logEntry);
}
```
- Prometheus, Micrometer 연동을 위한 메트릭 수집기
- `MetricCollectorCustomizer`로 등록 가능

---

## 사용 예시
```java
try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
    logEntryContextManager.addLog(new ExecutionLog(...));
}
```
- 로그는 Scope 단위로 수집
- 종료 시점에 자동 flush()

---

## 모듈 연계

- `monitoring-starter` → Spring Boot 자동 구성 + AOP
- `monitoring-starter-web` → Web 환경 필터/인터셉터
- `monitoring-metric` → Micrometer/Prometheus 메트릭 바인딩
