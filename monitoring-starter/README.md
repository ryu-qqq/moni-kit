# MoniKit Starter (v1.1.3)

## 🧭 개요

`monikit-starter`는 MoniKit Core의 구성 요소들을 Spring Boot 기반 프로젝트에 자동으로 통합해주는 모듈입니다.  
자동 구성 클래스(@Configuration / @AutoConfiguration)를 통해 traceId, 로그 수집 컨텍스트, 로거, AOP, 메트릭 등 핵심 기능들을 손쉽게 등록할 수 있습니다.

---

## ⚙️ 자동 구성 기능 요약

### 1. `ExecutionLoggingAutoConfiguration`
- 조건: `monikit.logging.log-enabled: true`
- AOP를 활성화하여 모든 메서드의 실행 시간 측정
- 설정된 DynamicLogRule 에 따라 로깅 결정

### 2. `LogEntryContextManagerConfig`
- `LogEntryContextManager` 등록
- `LogAddHook`, `LogFlushHook` 확장 지점 주입
- 기본 구현: `DefaultLogEntryContextManager`

### 3. `MetricCollectorHookAutoConfiguration`
- 조건: `monikit.metrics.metrics-enabled: true`
- `MetricCollectorLogAddHook` 자동 등록
- 커스터마이저 지원: `MetricCollectorCustomizer`

### 4. `MoniKitLoggingAutoConfiguration`
- `monikit.logging.*` 설정값 자동 바인딩
- 로깅 설정값 로드 및 초기화 시 출력

### 5. `MoniKitMetricsAutoConfiguration`
- `monikit.metrics.*` 설정값 자동 바인딩
- 메트릭 설정값 로드 및 초기화 시 출력

### 6. `Slf4jLoggerAutoConfiguration`
- SLF4J 기반 기본 `LogNotifier` 자동 등록
- 등록된 `LogSink` 목록에 따라 메시지 전송
- `Slf4jLogSink`가 없으면 기본값으로 포함시킴

### 7. `TraceIdProviderAutoConfiguration`
- 조건: `TraceIdProvider`가 미등록 시
- `MDCTraceIdProvider` 자동 등록
- SLF4J 기반 traceId MDC 연동

### 8. `ThreadContextHandlerAutoConfiguration`
- 조건: `ThreadContextHandler`가 미등록 시
- `MDCThreadContextHandler` 자동 등록
- MDC + LogEntryContextManager 스레드 전파 지원

---

## 🔁 기본 구현체

### MDC 기반 Trace ID

```java
public class MDCTraceIdProvider implements TraceIdProvider {
  private static final String TRACE_ID_KEY = "traceId";

  @Override
  public String getTraceId() {
    return Optional.ofNullable(MDC.get(TRACE_ID_KEY)).orElse("N/A");
  }

  @Override
  public void setTraceId(String traceId) {
    MDC.put(TRACE_ID_KEY, traceId);
  }

  @Override
  public void clear() {
    MDC.remove(TRACE_ID_KEY);
  }
}
```

---

### MDC 기반 스레드 컨텍스트 전파

```java
public class MDCThreadContextHandler extends DefaultThreadContextHandler {
  public Runnable propagateToChildThread(Runnable task) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    return () -> {
      try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
        if (contextMap != null) MDC.setContextMap(contextMap);
        try { task.run(); } finally { MDC.clear(); }
      }
    };
  }

  public <T> Callable<T> propagateToChildThread(Callable<T> task) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    return () -> {
      try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
        if (contextMap != null) MDC.setContextMap(contextMap);
        try { return task.call(); } finally { MDC.clear(); }
      }
    };
  }
}
```


### 동적 로깅 규칙(DynamicLogRule)에 기반한 실행 시간 측정 AOP
`ExecutionLoggingAspect는` **동적 로깅 규칙(DynamicLogRule)** 에 기반하여 메서드의 실행 시간을 측정하고 조건에 맞는 경우 로그를 기록하는 AOP입니다.

주요 기능:
- 설정 파일(`monikit.logging.dynamic-matching`)에서 정의된 조건에 따라 로깅 여부 결정.
- SpEL을 사용하여 클래스명, 메서드명, 실행 시간에 기반한 조건을 동적으로 설정 가능.
- 예외 발생 시 `ExceptionLog` 자동 기록.
- 실행 시간 기준으로 `ExecutionDetailLog` 기록.

```yml

monikit.logging:
    dynamic-matching:
    - classNamePattern: ".*Service"
    methodNamePattern: ".*Create"
    when: "#executionTime > 200"
    thresholdMillis: 200
    tag: "service-create-logging"
```

- `classNamePattern`: 클래스 이름 정규식
- `methodNamePattern`: 메서드 이름 정규식
- `when`: SpEL 조건식으로 실행 시간 기반 로깅 여부 결정
- `thresholdMillis`: 실행 시간이 이 값보다 길면 로깅
- `tag`: 로그에 태그를 추가

---

(c) 2024 Ryu Sangwon. MoniKit 프로젝트