# Monitoring-Core

## 📌 개요
`monitoring-core` 모듈은 애플리케이션에서 다양한 유형의 로그를 수집하고 관리하는 핵심 기능을 제공합니다.
이 모듈은 실행 시간 로깅, 데이터베이스 쿼리 로깅, 예외 로깅, HTTP 요청/응답 로깅 등을 지원합니다.

## 📁 패키지 구조
```
monitoring-core
│── src
│   ├── main
│   │   ├── java
│   │   │   ├── com.monikit.core
│   │   │   │   ├── AbstractLogEntry.java
│   │   │   │   ├── BatchJobLog.java
│   │   │   │   ├── DatabaseQueryLog.java
│   │   │   │   ├── ExceptionLog.java
│   │   │   │   ├── ExecutionDetailLog.java
│   │   │   │   ├── ExecutionTimeLog.java
│   │   │   │   ├── HttpInboundRequestLog.java
│   │   │   │   ├── HttpInboundResponseLog.java
│   │   │   │   ├── HttpOutboundRequestLog.java
│   │   │   │   ├── HttpOutboundResponseLog.java
│   │   │   │   ├── LogbackLogNotifier.java
│   │   │   │   ├── LogContextScope.java
│   │   │   │   ├── LogEntry.java
│   │   │   │   ├── LogEntryContext.java
│   │   │   │   ├── LogEntryContextManager.java
│   │   │   │   ├── LogLevel.java
│   │   │   │   ├── LogType.java
```

## 📦 주요 클래스 설명
### 1️⃣ **LogEntry (인터페이스)**
로그의 기본 인터페이스이며, 모든 로그 클래스는 이를 구현해야 합니다.
```java
public interface LogEntry {
    Instant getTimestamp();
    String getTraceId();
    LogLevel getLogLevel();
    LogType getLogType();
    String toJson();
}
```

### 2️⃣ **AbstractLogEntry**
모든 로그 클래스가 공통적으로 가져야 하는 속성을 포함하는 추상 클래스입니다.

### 3️⃣ **LogEntryContext**
현재 요청(스레드) 단위로 로그를 저장하고 관리하는 컨텍스트 클래스입니다.
```java
class LogEntryContext {
    private static final InheritableThreadLocal<Queue<LogEntry>> logThreadLocal =
        new InheritableThreadLocal<>() {
            @Override
            protected Queue<LogEntry> initialValue() {
                return new ConcurrentLinkedQueue<>();
            }
        };
    
    static void addLog(LogEntry logEntry) {
        logThreadLocal.get().add(logEntry);
    }
    static Queue<LogEntry> getLogs() {
        return new ConcurrentLinkedQueue<>(logThreadLocal.get());
    }
    static void clear() {
        logThreadLocal.remove();
    }
}
```

### 4️⃣ **LogEntryContextManager**
로그 컨텍스트를 관리하는 매니저 클래스이며, `LogEntryContext`를 컨트롤하는 역할을 합니다.
```java
public class LogEntryContextManager {
    private static final int MAX_LOG_SIZE = 1000;

    public static void addLog(LogEntry logEntry) {
        if (LogEntryContext.getLogs().size() >= MAX_LOG_SIZE) {
            LogbackLogNotifier.notify(LogLevel.WARN, "LogEntryContext cleared due to size limit");
            flush();
            LogEntryContext.clear();
        }
        LogEntryContext.addLog(logEntry);
    }

    public static void flush() {
        for (LogEntry log : LogEntryContext.getLogs()) {
            LogbackLogNotifier.notify(LogLevel.INFO, log.toJson());
        }
        LogEntryContext.clear();
    }
}
```

### 5️⃣ **LogContextScope**
`try-with-resources` 구문을 활용하여 자동으로 로그를 관리하는 클래스입니다.
```java
public class LogContextScope implements AutoCloseable {
    public LogContextScope() {}
    
    @Override
    public void close() {
        LogEntryContextManager.flush();
    }
}
```

## 🚀 사용 예제
### **1. 로그 수집 예제**
```java
try (LogContextScope context = new LogContextScope()) {
    LogEntryContextManager.addLog(new ExecutionTimeLog("trace-123", "OrderService", "processOrder", 300, LogLevel.INFO));
    LogEntryContextManager.addLog(new DatabaseQueryLog("trace-123", "SELECT * FROM orders", 120, "primary-db", "orders", "{}", 0, 10, LogLevel.DEBUG));
}
```
📌 **이렇게 하면 블록이 끝날 때 자동으로 flush()가 실행되어 로그가 처리됩니다.**

### **2. 부모-자식 스레드 컨텍스트 전파**
```java
Runnable childTask = LogEntryContextManager.propagateToChildThread(() -> {
    LogEntryContextManager.addLog(new ExecutionTimeLog("trace-123", "PaymentService", "processPayment", 200, LogLevel.INFO));
});
new Thread(childTask).start();
```
✅ **부모 스레드에서 생성한 `traceId`가 자식 스레드에서도 유지됩니다.**

---
## 📌 정리
✅ `monitoring-core`는 핵심 로깅 기능을 담당하는 모듈입니다.  
✅ `LogEntryContext`는 `ThreadLocal`을 활용하여 요청 단위로 로그를 관리합니다.  
✅ `LogEntryContextManager`는 로그 추가, 저장, flush 기능을 제공합니다.  
✅ `LogContextScope`는 try-with-resources를 활용하여 자동으로 로그를 관리합니다.  
✅ **모든 로그는 `LogEntry` 인터페이스를 구현하여 생성됩니다.**

🚀 **이제 `monitoring-starter`에서 이를 기반으로 실제 서비스에서 쉽게 사용할 수 있도록 지원합니다!**

