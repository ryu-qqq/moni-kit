# MoniKit Slf4j (v1.1.3)

## 🧭 개요

`monikit-slf4j`는 MoniKit의 구조화 로깅 시스템과 [SLF4J](http://www.slf4j.org/) 로깅 백엔드를 연결하는 확장 모듈입니다.  
별도의 설정 없이 SLF4J 기반 로그 전송을 지원하며, `LogSink` 및 `LogNotifier`에 대한 기본 구현체를 제공합니다.
- **최초 도입**: v1.1.3
  - `LogSinkCustomizer` 기반 확장 구조 도입
  - `Slf4jLogSink` → 자동 Fallback 등록 가능

---

## 📦 포함 컴포넌트

### `Slf4jLogger` (LogNotifier 구현체)

```java
@Slf4jLogger
public class Slf4jLogger implements LogNotifier {
    void notify(LogLevel level, String message);     // → SimpleLog 생성 후 전송
    void notify(LogEntry logEntry);                  // → LogSink 기반 분기 처리
}
```

- 모든 로그는 내부적으로 `TraceIdProvider`로부터 traceId를 주입받습니다.
- `SimpleLog` 형식의 메시지 로그와 구조화된 `LogEntry` 로그를 모두 처리할 수 있습니다.
- 등록된 `LogSink` 리스트를 순회하며 타입 기반 분기 전송을 수행합니다.

---

### `Slf4jLogSink` (LogSink 구현체)

```java
@Slf4jLogSink
public class Slf4jLogSink implements LogSink {
    boolean supports(LogType type);       // 항상 true
    void send(LogEntry logEntry);         // SLF4J 로그 출력
}
```

- 모든 로그 타입을 지원하며, 내부적으로 SLF4J의 `Logger`를 사용하여 로그를 출력합니다.
- 로그 레벨(`LogLevel`)에 따라 `info`, `warn`, `error`, `debug`로 자동 분기됩니다.

---

## ✅ 자동 등록

해당 모듈을 의존성에 추가하면 아래 자동 설정이 적용됩니다:

- `Slf4jLoggerAutoConfiguration`을 통해 SLF4J Logger가 `LogNotifier`로 자동 등록됩니다.
- 사용자 정의 `LogNotifier`가 등록되어 있지 않을 경우에만 적용됩니다.
- 등록된 `LogSink`가 없는 경우 `Slf4jLogSink`가 기본으로 등록됩니다.

---

## 💡 사용 예시

```yaml
# build.gradle
dependencies {
  implementation("com.monikit:monikit-slf4j:1.1.3")
}
```

```java
@Slf4j
@Service
public class ProductService {

    private final LogNotifier logNotifier;

    public ProductService(LogNotifier logNotifier) {
        this.logNotifier = logNotifier;
    }

    public void registerProduct() {
        logNotifier.notify(LogLevel.INFO, "신규 상품 등록 요청 수신됨");
    }
}
```

---

## 🧰 커스터마이징

### 로그 전송 대상 확장하기

```java
@Component
public class SlackLogSink implements LogSink {
    public boolean supports(LogType type) { return type == LogType.EXCEPTION; }
    public void send(LogEntry log) { slackClient.send(log.toString()); }
}
```

```java
@Component
public class SlackSinkCustomizer implements LogSinkCustomizer {
    public void customize(List<LogSink> sinks) {
        sinks.add(new SlackLogSink());
    }
}
```

---