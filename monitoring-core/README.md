# MoniKit Core (v1.1.3)

## 개요
MoniKit은 서버의 다양한 이벤트와 성능을 효과적으로 기록할 수 있도록 설계된 경량 로깅 프레임워크입니다. 
모든 로그는 구조화된 데이터를 생성하여 **ELK (Elasticsearch, Logstash, Kibana)** 및 **Prometheus**와 원활하게 연동될 수 있도록 설계되었습니다.

이 문서는 `monikit-core` 패키지의 핵심 구성 요소를 설명하며, 커스텀 로그 정의, 메트릭 수집기, 로그 전송 채널 및 후처리 훅 설계를 위한 기반 인터페이스를 안내합니다.

---

## 주요 변경 사항 (v1.1.3)

- `SimpleLog` 클래스 추가: 단순 메시지를 구조화 로그로 출력 가능
- `LogSinkCustomizer` 인터페이스 추가: 로그 전송 채널을 런타임에 커스터마이징 가능
- `MetricCollectorLogAddHook` 클래스 기본 제공: 로그 추가 시 자동 메트릭 수집
- `LogFlushHookCustomizer` 인터페이스 추가: 사용자 정의 flush 후처리를 손쉽게 확장 가능
- `LogSink`, `LogAddHook`, `LogFlushHook`에 대한 책임 및 용도 명확화

---

## 핵심 구성 요소

### 1. `LogEntry` 인터페이스
```java
public interface LogEntry {
    Instant getTimestamp();
    String getTraceId();
    LogType getLogType();
    LogLevel getLogLevel();
    String toString();
}
```
- 모든 로그 클래스는 이 인터페이스를 구현
- JSON 직렬화를 통해 로그 시스템에 전달

---

### 2. 주요 로그 클래스

| 카테고리 | 클래스 | 설명 |
|----------|--------|------|
| 실행 | `SimpleLog` | 일반 단순 메세지 로그 (traceId 포함) |
| 실행 | `ExecutionLog`, `ExecutionDetailLog` | 메서드 실행 시간, input/output, 임계값 기반 필터링 |
| 예외 | `ExceptionLog` | 예외 정보 및 타입 추적 |
| DB | `DatabaseQueryLog` | SQL 실행 정보 추적 |
| 배치 | `BatchJobLog`, `BatchStepLog`, `BatchChunkLog` | Spring Batch Job/Step/Chunk 실행 로그 |
| HTTP | `HttpInboundRequestLog`, `HttpOutboundResponseLog`, 등 | HTTP 요청/응답 구조화 로그 |

---

### 3. `LogSink`
```java
public interface LogSink {
    boolean supports(LogType logType);
    void send(LogEntry logEntry);
}
```
- 로그의 전송 채널을 정의하는 인터페이스 (예: Console, Slack, S3, Kafka 등)
- `supports(LogType)` 메서드로 전송 대상 필터링 가능
- `LogNotifier`에서 사용됨

---

### 4. `LogAddHook`
```java
public interface LogAddHook {
    void onAdd(LogEntry logEntry);
}
```
- `LogEntryContextManager.addLog()` 시점에 동작하는 실시간 후처리 훅
- Slack 알림, 상태 업데이트, 메트릭 증가 등의 목적
- LogSink와는 역할이 다르며, 부가처리에 최적화
- 기본 제공 구현체: `MetricCollectorLogAddHook`

#### 확장 방식: `LogAddHookCustomizer`
```java
public interface LogAddHookCustomizer {
    void customize(List<LogAddHook> hooks);
}
```
- LogAddHook 리스트를 동적으로 확장하거나 수정할 수 있도록 지원하는 커스터마이저 인터페이스
- 사용자는 여러 Hook을 조합하거나 조건에 따라 적용할 수 있음
- `LogEntryContextManager` 초기화 시점에 적용되며, 후처리 흐름을 조립할 때 유용함

#### 예시
```java
@Component
public class CustomLogAddHookCustomizer implements LogAddHookCustomizer {
    @Override
    public void customize(List<LogAddHook> hooks) {
        hooks.add(new SlackAlertAddHook());
    }
}
```
- 기본 제공 구현체: `MetricCollectorLogAddHook`

---

### 5. `LogFlushHook`
```java
public interface LogFlushHook {
    void onFlush(List<LogEntry> logEntries);
}
```
- `LogEntryContextManager.flush()` 시점에 전체 로그 목록 대상으로 실행되는 후처리 훅
- DB 저장, 집계 처리, 압축 및 S3 업로드 등 배치성 로직에 최적
- **MoniKit은 기본적으로 `LogFlushHook`을 등록하지 않으며**, flush 후처리가 필요한 경우 사용자가 `LogFlushHook`을 직접 구현하고, 해당 인스턴스를 `LogFlushHookCustomizer`를 통해 명시적으로 추가해야 합니다.
- hook이 전혀 등록되지 않으면 flush 동작은 건너뛰어지며, 아무 작업도 수행하지 않습니다.

---

### 6. `LogFlushHookCustomizer`
```java
public interface LogFlushHookCustomizer {
    void customize(List<LogFlushHook> hooks);
}
```

- flush 후처리 로직을 동적으로 조합하거나 추가할 수 있는 확장 포인트입니다.
- MoniKit의 기본 DefaultLogEntryContextManager는 이 커스터마이저를 자동으로 인식하고, 내부적으로 초기화한 List<LogFlushHook>에 후처리 훅을 주입합니다.
- 만약 flush 동작이 필요한데 직접 LogFlushHook을 빈으로 등록하지 않았다면, 이 커스터마이저를 활용해 필요한 훅을 등록할 수 있습니다.

#### 사용 예시
```java
@Component
public class SomethingFlushHookCustomizer implements LogFlushHookCustomizer {
    @Override
    public void customize(List<LogFlushHook> hooks) {
        hooks.add(new SomethingFlushHook());
    }
}
```

---

### 7. `LogNotifier`
```java
public interface LogNotifier {
    void notify(LogLevel logLevel, String message);
    void notify(LogEntry logEntry);
}
```
- 실제 로그 전송을 트리거하는 컴포넌트
- 내부적으로 `LogSink`를 통해 전송 분기 처리
- 기본 구현: `Slf4jLogger`

---

### 8. `LogSinkCustomizer`
```java
public interface LogSinkCustomizer {
    void customize(List<LogSink> sinks);
}
```
- 사용자가 로그 전송 Sink를 동적으로 추가하거나 필터링할 수 있는 확장 포인트
- Slf4jLoggerAutoConfiguration 에서 활용됨

---

### 9. `LogEntryContextManager`
```java
public interface LogEntryContextManager {
    void addLog(LogEntry logEntry);
    void flush();
    void clear();
}
```
- 요청 단위의 로그 수집, 전송, 후처리를 관리하는 핵심 매니저
- 기본 구현: `DefaultLogEntryContextManager`
- 내부적으로 `LogAddHook`, `LogFlushHook`, `LogNotifier` 연동

---

### 10. `TraceIdProvider`, `ThreadContextHandler`
- TraceId를 스레드 간 안전하게 전파하는 유틸리티 인터페이스
- MDC 기반 구현체 제공 (예: `MDCTraceIdProvider`, `MDCThreadContextHandler`)

---

### 11. `MetricCollector`, `MetricCollectorCustomizer`
```java
public interface MetricCollector<T extends LogEntry> {
    boolean supports(LogType type);
    void record(T logEntry);
}

public interface MetricCollectorCustomizer {
    void customize(List<MetricCollector<? extends LogEntry>> collectors);
}
```
- 로그 기반 메트릭 수집기 및 사용자 정의 확장 인터페이스
- Prometheus, Micrometer 등과의 통합 용도

---

## 사용 가이드 요약

```java
try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
    logEntryContextManager.addLog(new ExecutionLog(...));
    }
```

- 반드시 try-with-resources 사용
- 수동 `flush()` 대신 `LogContextScope` 사용을 권장

---

## 더 알아보기

- [monitoring-starter-batch](../monitoring-starter-batch)
- [monitoring-starter-web](../monitoring-starter-web)
- [monitoring-metric](../monitoring-metric)

---

(c) 2024 Ryu-qqq. MoniKit 프로젝트

