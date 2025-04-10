# MoniKit Core (v1.1.0)

## 개요
MoniKit은 서버의 다양한 이벤트와 성능을 효과적으로 기록할 수 있도록 설계된 경량 로깅 프레임워크입니다. 모든 로그는 구조화된 데이터를 생성하여 **ELK (Elasticsearch, Logstash, Kibana)** 및 **Prometheus**와 원활하게 연동될 수 있도록 설계되었습니다.

이 문서는 `monikit-core` 패키지의 핵심 구성 요소를 설명하며, 커스텀 로그 정의 및 수집기 설계를 위한 기반 인터페이스를 안내합니다.

---

## 주요 변경 사항 (v1.1.0)

- `ExecutionLog` 계층 도입: 요약/상세 로그 구조 통합 및 임계값 기반 필터링 지원
- `HttpLogEntry` 인터페이스 도입: 모든 HTTP 로그 공통화
- `ErrorCategoryClassifier` 폐기: `ExceptionLog` 단일 진입점으로 예외 추적 구조 단순화
- `LogSink` 기반 구조화: `DefaultLogNotifier`에서 전략적 로그 분기 가능
- `ErrorLogNotifier` 제거
- `LogContextScope` 안정성 강화

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
모든 로그 클래스는 이 인터페이스를 구현하며, 로그 데이터는 JSON 형태로 직렬화되어 저장 및 분석됩니다.

---

### 2. 주요 로그 클래스

| 카테고리 | 클래스 | 설명 |
|----------|--------|------|
| 실행 | `ExecutionLog`, `ExecutionDetailLog` | 메서드 실행 시간, input/output, 임계값 기반 분기 |
| 예외 | `ExceptionLog` | 예외 정보 및 타입 추적 |
| DB | `DatabaseQueryLog` | SQL 실행 정보 추적 |
| 배치 | `BatchJobLog`, `BatchStepLog`, `BatchChunkLog` | Job/Step/Chunk 단위 실행 정보 |
| HTTP | `HttpInboundRequestLog`, `HttpInboundResponseLog`, `HttpOutboundRequestLog`, `HttpOutboundResponseLog` | 모든 HTTP 요청/응답 흐름 추적 |

---

### 3. `HttpLogEntry` 인터페이스

```java
public interface HttpLogEntry extends LogEntry {
    String getUri();
    String getMethod();
    int getStatusCode();
    Map<String, String> getHeaders();
}
```

- 모든 HTTP 로그에 공통 필드 제공
- `LogSink`나 필터에서 `instanceof` 검사로 쉽게 필터링 가능

---

### 4. `LogEntryContextManager`

```java
public interface LogEntryContextManager {
    void addLog(LogEntry logEntry);
    void flush();
    void clear();
}
```

- 요청 단위로 로그를 수집 및 전송
- 기본 구현체: `DefaultLogEntryContextManager`

---

### 5. `LogNotifier` + `LogSink`

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

- `DefaultLogNotifier`는 `LogSink` 리스트에 따라 로그 분기
- 예: SlackSink, ConsoleSink, FileSink 등 확장 가능

---

### 6. `TraceIdProvider`, `ThreadContextHandler`

- 스레드 간 traceId 전달을 위한 유틸리티
- 기본 구현체 외에 `MDCThreadContextHandler`로 확장 가능

---

### 7. `MetricCollector`

```java
public interface MetricCollector<T extends LogEntry> {
    boolean supports(LogType type);
    void record(T logEntry);
}
```

- 로그에 대한 메트릭 측정 및 수집기 역할
- Prometheus, Micrometer 등과 통합 가능

---

## 사용 가이드 요약

```java
try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
    // 실행 중 로그 수집
    logEntryContextManager.addLog(new ExecutionLog(...));
}
```

- 반드시 try-with-resources 사용
- 수동 `flush()` 대신 `LogContextScope` 사용 권장

---

## 더 알아보기

- [monitoring-starter-batch](../monitoring-starter-batch)
- [monitoring-starter-web](../monitoring-starter-web)
- [monitoring-metric](../monitoring-metric)

---

(c) 2024 Ryu-qqq. MoniKit 프로젝트