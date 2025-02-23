# Monitoring-Core

## 개요
MoniKit은 서버의 다양한 이벤트와 성능을 효과적으로 기록할 수 있도록 설계된 로깅 라이브러리입니다. 모든 로그는 구조화된 데이터를 생성하여 **ELK (Elasticsearch, Logstash, Kibana) 및 Prometheus**와 원활하게 연동될 수 있도록 설계되었습니다.

이 문서는 `monikit.core` 패키지의 핵심 구성 요소를 설명하며, 사용자가 직접 커스텀 로그 포맷을 정의할 수 있도록 가이드합니다.

### **로그**
### 1. LogEntry 인터페이스
```java
public interface LogEntry {
    Instant getTimestamp();
    String getTraceId();
    LogType getLogType();
    LogLevel getLogLevel();
    String toString();
}
```
모든 로그 엔트리는 `LogEntry` 인터페이스를 구현해야 합니다. 이는 **로그의 일관성을 유지하고, 검색 필터링을 용이하게 하도록 설계**되었습니다.

- `getTimestamp()`: 로그가 생성된 시간 (UTC 기준)
- `getTraceId()`: 동일한 요청 내에서 발생한 로그를 추적하기 위한 ID
- `getLogType()`: 로그 유형 (예: EXECUTION_TIME, BUSINESS_EVENT, EXCEPTION 등)
- `getLogLevel()`: 로그의 심각도 (TRACE, DEBUG, INFO, WARN, ERROR)
- `toString()`: JSON 형태의 문자열 변환

사용자는 이 인터페이스를 직접 구현하여 **커스텀 로그 타입을 정의**할 수도 있습니다.

`LogEntry` 인터페이스는 MoniKit의 핵심이며, 모든 로그는 이를 기반으로 작성됩니다. 인터페이스를 확장하여 새로운 로그 타입을 정의할 수도 있으며, 기본적으로 제공되는 여러 로그 클래스가 존재합니다.

---



### 2. 기본 제공되는 로그 클래스
MoniKit은 다양한 로깅 요구 사항을 충족하기 위해 몇 가지 표준 로그 클래스를 제공합니다. 다음은 기본 제공되는 로그 클래스 목록입니다.

- `BatchJobLog`: 배치 작업 실행 정보를 기록
- `DatabaseQueryLog`: 데이터베이스 쿼리 실행 정보를 기록
- `ExceptionLog`: 애플리케이션 내에서 발생한 예외를 기록 **ErrorCategory** 을 활용하여 에러의 내용을 카테고리화 가능 
- `ExecutionDetailLog`: 메서드 실행 상세 정보를 기록
- `ExecutionTimeLog`: 메서드 실행 시간을 기록
- `HttpInboundRequestLog`: 외부에서 들어오는 HTTP 요청을 기록
- `HttpInboundResponseLog`: 외부에서 들어오는 HTTP 요청의 응답을 기록
- `HttpOutboundRequestLog`: 내부에서 나가는 HTTP 요청을 기록
- `HttpOutboundResponseLog`: 내부에서 나가는 HTTP 요청의 응답 기록

이들 클래스는 `AbstractLogEntry`를 확장하여 구현되었으며, 필요한 경우 직접 커스텀 로그 클래스를 만들어 사용할 수도 있습니다.

더 자세한 사항이 궁금하다면 코드에서 직접 구현체를 확인해보세요

---


### 3. LogEntryContextManager (로그 컨텍스트 관리)
`LogEntryContextManager`는 `LogEntryContext`를 관리하는 인터페이스로, 로그를 추가, 조회, 삭제하는 기능을 제공합니다.

```java
public interface LogEntryContextManager {
    void addLog(LogEntry logEntry);
    void flush();
    void clear();
}
```
### 기본 구현체: `DefaultLogEntryContextManager`
기본적으로 `DefaultLogEntryContextManager`가 빈으로 등록됩니다.

```java
public class DefaultLogEntryContextManager implements LogEntryContextManager {
    private static final int MAX_LOG_SIZE = 300;
    private final LogNotifier logNotifier;
    private final ErrorLogNotifier errorLogNotifier;

    public DefaultLogEntryContextManager(LogNotifier logNotifier, ErrorLogNotifier errorLogNotifier) {
        this.logNotifier = logNotifier;
        this.errorLogNotifier = errorLogNotifier;
    }

    @Override
    public void addLog(LogEntry logEntry) {
        if (LogEntryContext.size() >= MAX_LOG_SIZE) {
            logNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
            flush();
        }
        LogEntryContext.addLog(logEntry);
    }

    @Override
    public void flush() {
        for (LogEntry log : LogEntryContext.getLogs()) {
            logNotifier.notify(log);
            if (log.getLogLevel().isEmergency() && log instanceof ExceptionLog exceptionLog) {
                errorLogNotifier.onErrorLogDetected(exceptionLog);
            }
        }
        clear();
    }

    @Override
    public void clear() {
        LogEntryContext.clear();
    }
}
```
### 주요 특징
- 요청 단위로 로그를 관리하며, 일정 크기(`MAX_LOG_SIZE`) 이상이 되면 자동으로 `flush()` 호출
- `flush()`를 통해 수집된 로그를 저장소 또는 모니터링 시스템으로 전송
- `ErrorLogNotifier`를 활용하여 긴급 오류 발생 시 추가 조치 수행 가능

사용자가 별도로 설정하지 않으면 `DefaultLogEntryContextManager`가 자동으로 빈으로 등록됩니다. 필요하면 `LogEntryContextManager`를 구현하여 커스텀 로깅 관리를 설정할 수 있습니다.



---

## 4. LogNotifier & ErrorLogNotifier
MoniKit은 로그를 출력하고, 에러 로그를 감지하는 두 개의 주요 인터페이스를 제공합니다.

### LogNotifier (로그 출력)
```java
public interface LogNotifier {
    void notify(LogLevel logLevel, String message);
    void notify(LogEntry logEntry);
}
```
로그를 외부 시스템에 전달하거나, 저장하는 역할을 합니다.
사용자가 직접 구현할 수 있으며, 기본적으로 제공되는 구현체는 `DefaultLogNotifier`입니다.

#### DefaultLogNotifier (기본 구현체)
```java
public class DefaultLogNotifier implements LogNotifier {
    @Override
    public void notify(LogLevel logLevel, String message) {
        System.out.println(message);
    }
    @Override
    public void notify(LogEntry logEntry) {
        System.out.println(logEntry.toString());
    }
}
```
기본적으로 `System.out.println()`을 사용하여 로그를 출력합니다.

### ErrorLogNotifier (에러 로그 감지)
```java
public interface ErrorLogNotifier {
    void onErrorLogDetected(ExceptionLog logEntry);
}
```
예외 로그를 감지하고 추가적인 처리를 수행하는 역할을 합니다.
기본적으로 제공되는 구현체는 `DefaultErrorLogNotifier`입니다.

#### DefaultErrorLogNotifier (기본 구현체)
```java
public class DefaultErrorLogNotifier implements ErrorLogNotifier {
    @Override
    public void onErrorLogDetected(ExceptionLog logEntry) {
        System.out.printf("Error, %s%n", logEntry.toString());
    }
}
```
에러 로그가 감지되면 `System.out.printf()`를 사용하여 출력합니다.

---


## 5. ThreadContextHandler (스레드 컨텍스트 전파)
멀티스레드 환경에서 로그 컨텍스트를 유지하려면 `ThreadContextHandler`를 사용해야 합니다.

```java
public interface ThreadContextHandler {
    Runnable propagateToChildThread(Runnable task);
    <T> Callable<T> propagateToChildThread(Callable<T> task);
    <T> ThrowingCallable<T> propagateToChildThreadThrowable(ThrowingCallable<T> task);
}
```
### 기본 구현체: `DefaultThreadContextHandler`
기본적으로 `DefaultThreadContextHandler`가 빈으로 등록됩니다. 이 클래스는 부모 스레드의 컨텍스트를 자식 스레드로 복사하여 유지하는 역할을 합니다.

```java
public class DefaultThreadContextHandler implements ThreadContextHandler {
    @Override
    public Runnable propagateToChildThread(Runnable task) {
        return ThreadContextPropagator.propagateToChildThread(task);
    }
    @Override
    public <T> Callable<T> propagateToChildThread(Callable<T> task) {
        return ThreadContextPropagator.propagateToChildThread(task);
    }
}
```

### 사용자 정의 구현 예제 (MDC 연동 & 자동 플러시)
`MDCThreadContextHandler`는 기본 구현체와 다르게 **스레드 컨텍스트를 복사하지 않고 유지하면서 `AutoCloseable`을 활용하여 자동으로 로그를 플러시**합니다.

```java
@Component
@Primary
public class MDCThreadContextHandler extends DefaultThreadContextHandler {
    private final LogEntryContextManager logEntryContextManager;

    public MDCThreadContextHandler(LogEntryContextManager logEntryContextManager) {
        this.logEntryContextManager = logEntryContextManager;
    }

   public Runnable propagateToChildThread(Runnable task) {
      Map<String, String> contextMap = MDC.getCopyOfContextMap();

      return () -> {
         try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
            if (contextMap != null) {
               MDC.setContextMap(contextMap);
            }
            try {
               task.run();
            } finally {
               MDC.clear();
            }
         }
      };
   }

   public <T> Callable<T> propagateToChildThread(Callable<T> task) {
      Map<String, String> contextMap = MDC.getCopyOfContextMap();

      return () -> {
         try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
            if (contextMap != null) {
               MDC.setContextMap(contextMap);
            }
            try {
               return task.call();
            } finally {
               MDC.clear();
            }
         }
      };
   }
}
```

---

## 주의 사항
MoniKit을 사용할 때 **반드시 `LogContextScope`를 활용하여 로그 컨텍스트를 관리**해야 합니다. 그렇지 않으면 **스레드 로컬 변수 관리의 어려움으로 인해 휴먼 에러가 발생할 수 있습니다.**

### ✅ 올바른 사용 예시
```java
try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
    // 로그 추가 및 컨텍스트 유지
    task.run();
} // 자동으로 flush() 호출
```

### ❌ 잘못된 사용 예시
```java
// LogContextScope 없이 실행 -> 컨텍스트가 유지되지 않음
logEntryContextManager.clear();
task.run();
logEntryContextManager.flush(); // 수동 호출 필요 (휴먼 에러 위험)
```

---


## 6. SQL 쿼리 로깅

MoniKit은 SQL 실행 정보를 효과적으로 로깅하기 위한 여러 기능을 제공합니다.
PreparedStatement`를 감싸는 프록시 객체를 활용하여 SQL 실행 시간을 측정하고 바인딩된 파라미터 값을 로깅하는 구조로 설계되었습니다.

### QueryLoggingService (쿼리 로깅 인터페이스)
SQL 실행 로그를 기록하고 메트릭을 수집하는 역할을 합니다.
사용자가 직접 구현하여 원하는 방식으로 쿼리 로그를 기록할 수 있습니다.

```java
public interface QueryLoggingService {
    void logQuery(String traceId, String sql, String parameter, long executionTime, int rowsAffected);
}
```

### DefaultQueryLoggingService (기본 구현체)
SQL 실행 정보를 `LogEntryContextManager`를 통해 저장하고, `MetricCollector`를 이용해 SQL 실행 관련 메트릭을 수집합니다.
```java
public class DefaultQueryLoggingService implements QueryLoggingService {
    private final LogEntryContextManager logEntryContextManager;
    private final MetricCollector metricCollector;
    private final DataSourceProvider dataSourceProvider;
    private final long slowQueryThresholdMs;
    private final long criticalQueryThresholdMs;

    public DefaultQueryLoggingService(LogEntryContextManager logEntryContextManager,
                                      MetricCollector metricCollector,
                                      DataSourceProvider dataSourceProvider,
                                      long slowQueryThresholdMs,
                                      long criticalQueryThresholdMs) {
        this.logEntryContextManager = logEntryContextManager;
        this.metricCollector = metricCollector;
        this.dataSourceProvider = dataSourceProvider;
        this.slowQueryThresholdMs = slowQueryThresholdMs;
        this.criticalQueryThresholdMs = criticalQueryThresholdMs;
    }

    @Override
    public void logQuery(String traceId, String sql, String parameter, long executionTime, int rowsAffected) {
        String dataSourceName = dataSourceProvider.getDataSourceName();

        LogLevel logLevel = QueryPerformanceEvaluator.evaluate(
            executionTime, slowQueryThresholdMs, criticalQueryThresholdMs
        );

        DatabaseQueryLog logEntry = DatabaseQueryLog.create(
            traceId, sql, executionTime, dataSourceName, parameter, rowsAffected, -1, logLevel
        );

        logEntryContextManager.addLog(logEntry);
        metricCollector.recordQueryMetrics(sql, executionTime, dataSourceName);
    }
}
```


### LoggingPreparedStatement (SQL 실행 로깅)
`PreparedStatementWrapper`를 상속받아 SQL 실행 시간 및 파라미터를 로깅하는 역할을 합니다.
```java
public class LoggingPreparedStatement extends PreparedStatementWrapper {
    private final String traceId;
    private final String sql;
    private final SqlParameterHolder holder;
    private final QueryLoggingService queryLoggingService;

    public LoggingPreparedStatement(PreparedStatement delegate, String traceId, String sql,  
                                    SqlParameterHolder holder, QueryLoggingService queryLoggingService) {
        super(delegate);
        this.traceId = traceId;
        this.sql = sql;
        this.holder = holder;
        this.queryLoggingService = queryLoggingService;
    }

    @Override
    public void setObject(int parameterIndex, Object x) throws SQLException {
        holder.addParameter(x);
        super.setObject(parameterIndex, x);
    }

    @Override
    public boolean execute() throws SQLException {
        long start = System.currentTimeMillis();
        boolean result = super.execute();
        long executionTime = System.currentTimeMillis() - start;
        queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, -1);
        return result;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        long start = System.currentTimeMillis();
        ResultSet result = super.executeQuery();
        long executionTime = System.currentTimeMillis() - start;
        queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, -1);
        return result;
    }

    @Override
    public int executeUpdate() throws SQLException {
        long start = System.currentTimeMillis();
        int rowsAffected = super.executeUpdate();
        long executionTime = System.currentTimeMillis() - start;
        queryLoggingService.logQuery(traceId, sql, holder.getCurrentParameters(), executionTime, rowsAffected);
        return rowsAffected;
    }

    @Override
    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            holder.close();
        }
    }
}
```


### SqlParameterHolder (SQL 바인딩 파라미터 관리)
각 스레드별로 `ThreadLocal`을 활용하여 SQL 실행 시 바인딩된 값을 안전하게 관리합니다.
```java
public class SqlParameterHolder implements AutoCloseable {
    private final ThreadLocal<List<Object>> parametersHolder = ThreadLocal.withInitial(ArrayList::new);

    public SqlParameterHolder() {
        parametersHolder.get().clear();
    }

    public void addParameter(Object parameter) {
        parametersHolder.get().add(parameter);
    }

    public String getCurrentParameters() {
        return parametersHolder.get().toString();
    }

    @Override
    public void close() {
        parametersHolder.get().clear();
    }
}
```

###  실행 흐름
1️⃣ **SQL 실행 전** → `LoggingPreparedStatement`가 `SqlParameterHolder`를 초기화함.
2️⃣ **SQL 실행 중** → `setObject()` 등의 메서드가 호출될 때 `SqlParameterHolder`에 값 저장.
3️⃣ **SQL 실행 후** → `execute()` 또는 `executeQuery()` 실행 시간이 측정되며 `QueryLoggingService`를 통해 로그 기록.
4️⃣ **SQL 실행 완료 후** → `close()` 메서드에서 `SqlParameterHolder`를 정리하여 메모리 누수 방지.


## 7. MetricCollector
메트릭 수집을 위한 공통 인터페이스로, HTTP 요청 및 SQL 쿼리 실행 메트릭을 수집할 수 있도록 설계되었습니다.
사용자가 직접 구현하여 원하는 메트릭 시스템과 연동할 수 있습니다.
```java
public interface MetricCollector {
    void recordHttpRequest(String method, String uri, int statusCode, long duration);
    void recordQueryMetrics(String sql, long executionTime, String dataSourceName);
}
```
