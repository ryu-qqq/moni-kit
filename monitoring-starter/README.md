# monikit-starter

> 모니터링과 로깅을 위한 자동 설정 스타터  
> `Spring Boot` 환경에서 AOP, TraceId, Metric, Filter, Batch 리스너를 자동으로 설정합니다.

---

## ✅ 의존성 추가

**Gradle**

```groovy
implementation "com.github.ryu-qqq.moni-kit:monikit-starter:1.1.0"
```

이 스타터 하나로 아래의 의존성이 자동 포함됩니다:
- `monikit-core`
- `monikit-config`
- AOP, Servlet, WebMVC, Spring Boot Starter

---

## ⚙️ 자동 구성되는 기능

### 1. Execution AOP (메서드 실행 로깅)

```java
@Aspect
public class ExecutionLoggingAspect { ... }
```

- `@Service`, `@Repository`, `@Controller`, `@RestController` 대상
- 실행 시간 측정 → 설정된 임계값 초과 시 상세 로그
- 정상/에러 흐름 모두 `LogEntryContextManager`에 기록

> ⛔ 비활성화하려면 `monikit.logging.detailed-logging=false`

---

### 2. Filter 등록 (Servlet)

- `TraceIdFilter`: 요청에 traceId 삽입 및 전달
- `LogContextScopeFilter`: 요청 단위로 로그 컨텍스트 관리

```yaml
monikit.logging:
  trace-enabled: true
  log-enabled: true
```

---

### 3. MetricCollector 자동 Hook

- `MetricCollectorLogAddHook` 자동 등록
- `monikit.metrics.metrics-enabled=true` 조건에서만 작동

---

### 4. LogEntryContextManager 자동 등록

- `DefaultLogEntryContextManager` 제공
- `LogNotifier`, `TraceIdProvider`, `LogAddHook`, `LogFlushHook` 등 필요한 기본 컴포넌트 자동 주입

---

### 5. 설정 클래스 자동 등록

- `MoniKitLoggingProperties`
- `MoniKitMetricsProperties`

---

## 🧩 확장 포인트

### LogSink
- 로그 타입별 전송 전략 커스터마이징

### LogAddHook / LogFlushHook
- 로그가 추가/플러시될 때 후처리 커스터마이징

### MetricCollector
- Prometheus, StatsD 등 연동용 커스텀 수집기 정의 가능

---

## 🔌 관련 스타터

- `monikit-starter-web`: Web 로그 수집 필터
- `monikit-starter-batch`: Spring Batch Job/Step 리스너 자동 설정
- `monikit-starter-jdbc`: SQL 실행 시간, 슬로우 쿼리 메트릭

---

## 📜 설정 예시 (application.yml)

```yaml
monikit:
  logging:
    log-enabled: true
    trace-enabled: true
    detailed-logging: true
    summary-logging: true
    threshold-millis: 300
  metrics:
    metrics-enabled: true
    query-metrics-enabled: true
    http-metrics-enabled: true
    slow-query-threshold-ms: 1500
```

---
