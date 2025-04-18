# MoniKit Metric (v1.1.0)

## 개요
`monikit-metric`은 MoniKit 프레임워크의 메트릭 수집 기능을 담당하는 모듈입니다. 이 모듈은 `LogEntry`를 기반으로 정의된 로그 데이터를 분석하여 Prometheus, Grafana와 같은 모니터링 툴에서 활용 가능한 메트릭 데이터를 수집하고 전송합니다.

주요 수집 대상은 HTTP 요청/응답, 데이터베이스 쿼리, 배치 처리 등이며, `MetricCollector` 인터페이스를 통해 유연하게 확장 가능하도록 설계되었습니다.

---

## 주요 기능

### ✅ 로그 기반 메트릭 수집 자동화
- `MetricCollector<T extends LogEntry>` 구현체 등록 시 자동 수집
- LogType 별로 collector 자동 라우팅 (`MetricCollectorLogAddHook` 기반)
- 다양한 로그 타입 지원: `HttpOutboundResponseLog`, `DatabaseQueryLog`, `BatchStepLog`, `ExecutionLog` 등

### ✅ Micrometer 기반 수집
- Prometheus export를 위한 `io.micrometer.core.instrument.MeterRegistry` 연동
- `Timer`, `Counter`, `Gauge` 등 다양한 메트릭 타입 지원

### ✅ AutoConfiguration 기반 자동 빈 등록
- `@ConditionalOnProperty`로 설정 기반 등록 제어
- `@ConditionalOnBean`, `@ConditionalOnClass` 등 조건 조합으로 의존성 안전 확보

---

## 메트릭 Collector 구조

### `MetricCollector<T extends LogEntry>`
```java
public interface MetricCollector<T extends LogEntry> {
    boolean supports(LogType logType);
    void record(T logEntry);
}
```

- `LogType` 기반으로 수집 대상을 선택하고
- `record()`에서 Micrometer로 메트릭 수집

### 자동 등록되는 Collector 예시
| 클래스 | 설명 |
|--------|------|
| `DatabaseQueryMetricCollector` | SQL 실행 횟수 및 응답 시간 수집 |
| `HttpInboundResponseMetricCollector` | 내부 API 응답 결과 수집 |
| `HttpOutboundResponseMetricCollector` | 외부 API 호출 성공/실패 추적 |
| `BatchStepMetricRecorder` | 배치 Step 처리량 및 skip 수 추적 |


---

## 자동 설정 조건

```yaml
monikit:
  metrics:
    metrics-enabled: true
    query-metrics-enabled: true
    http-metrics-enabled: true
    slow-query-threshold-ms: 2000
    query-sampling-rate: 10
```

- 설정 값에 따라 메트릭 수집기 및 recorder 빈이 자동 등록됨
- `MetricCollectorAutoConfiguration`, `MetricCollectorLogAddHook`가 핵심

---

## 통합 흐름 구조도

```
[ LogEntry 발생 ]
       ↓
[ LogEntryContextManager.addLog(log) ]
       ↓
[ MetricCollectorLogAddHook.onAdd(log) ]
       ↓
[ 등록된 MetricCollector<T> 가 supports(LogType) 매칭 시 record(log) 호출 ]
       ↓
[ Micrometer (MeterRegistry) 를 통해 메트릭 전송 ]
```

---

## 확장 방법

- 새로운 로그 타입에 대한 메트릭 수집기를 만들고 싶을 경우:

```java
@Component
public class CustomExecutionMetricCollector implements MetricCollector<ExecutionLog> {

    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.EXECUTION;
    }

    @Override
    public void record(ExecutionLog log) {
        meterRegistry.timer("execution_duration", "method", log.getMethodName())
                     .record(log.getExecutionTime(), TimeUnit.MILLISECONDS);
    }
}
```

- 등록만 하면 `LogAddHook`에서 자동 연결됨

---

## 참고 모듈

- [`monikit-core`](../monikit-core)
- [`monikit-config`](../monikit-config)
- [`monikit-starter`](../monikit-starter)
- [`monikit-starter-batch`](../monikit-starter-batch)

---

(c) 2024 Ryu-qqq. MoniKit Metric 모듈

