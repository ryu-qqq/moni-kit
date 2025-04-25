# MoniKit Core (v1.1.2)

## 📌 개요
MoniKit은 서버의 다양한 이벤트와 성능을 효과적으로 기록할 수 있도록 설계된 **경량 구조화 로깅 프레임워크**입니다.

모든 로그는 `LogEntry` 인터페이스를 구현하는 구조화 객체로 표현되며,
이는 **ELK (Elasticsearch, Logstash, Kibana)** 및 **Prometheus**와 자연스럽게 연동될 수 있도록 설계되었습니다.

이 문서는 `monikit-core` 패키지의 아키텍처와 구성 요소, 그리고 도메인 계층 설계 철학을 설명합니다.

---

## 🚧 설계 철학

- `monikit-core`는 **어떠한 외부 프레임워크에도 의존하지 않는 순수 자바 코드로만 구성**되어 있습니다.
- 스프링이나 마이크로서비스 환경에서 사용할 경우, 별도의 `starter-*` 모듈을 통해 확장됩니다.
- **컨텍스트 전파, 로그 수집, 메트릭 기록, 전송 전략**을 모두 도메인 단위로 나누어 응집도 높은 설계를 따릅니다.
- 모든 클래스는 **어디서든 임포트해서 사용할 수 있어야 하며**, 실행 주체(Runnable)는 core에 포함되지 않습니다.

---

## ✅ 디렉토리 구성 및 책임

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

## 모듈 연계 안내

- `monitoring-starter-web` → Web 환경에서 AOP + 필터 기반 적용
- `monitoring-starter-batch` → Spring Batch와 통합
- `monitoring-metric` → Prometheus 기반 메트릭 바인딩

---

(c) 2024 Ryu-qqq. MoniKit 프로젝트
