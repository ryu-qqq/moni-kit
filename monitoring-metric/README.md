# MoniKit Metric (v1.1.2)

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
| 클래스 | 설명               |
|--------|------------------|
| `DatabaseQueryMetricCollector` | SQL 실행 횟수 및 응답 시간 수집 |
| `HttpInboundResponseMetricCollector` | 내부 API 응답 결과 수집  |
| `HttpOutboundResponseMetricCollector` | 외부 API 호출 성공/실패 추적 |
| `ExecutionDetailMetricCollector` | 메서드 실행 시간 수집     |


---

## 자동 설정 조건

```yaml
monikit:
  metrics:
    metrics-enabled: true
    query-metrics-enabled: true
    http-metrics-enabled: true
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

<details>
<summary><strong>Grafana 쿼리 예시 - SQL 메트릭 </strong></summary>

# 📊 Grafana 쿼리 예시 - SQL 메트릭 (`monitoring-metric`)

> 이 문서는 MoniKit에서 수집하는 SQL 관련 메트릭(`sql_query_total`, `sql_query_duration`)을 기반으로
> Grafana 대시보드에서 활용할 수 있는 PromQL 쿼리 예시를 제공합니다.

---

## ✅ 쿼리별 실행 횟수
```promql
sum by (query) (sql_query_total)
```

### 🔎 특정 쿼리의 실행 추이
```promql
increase(sql_query_total{query="select_products"}[5m])
```

---

## 📊 데이터소스별 쿼리 분포
```promql
sum by (dataSource) (sql_query_total)
```

---

## ⏱️ 쿼리 실행 시간 분석

### 평균 실행 시간 (쿼리별)
```promql
rate(sql_query_duration_sum[5m]) / rate(sql_query_duration_count[5m])
```

### p95, p99 실행 시간 시각화
```promql
sql_query_duration{quantile="0.95"}
```

```promql
sql_query_duration{quantile="0.99"}
```

---

## 🔝 상위 느린 쿼리 TOP5
```promql
topk(5, rate(sql_query_duration_sum[5m]) / rate(sql_query_duration_count[5m]))
```

---

## 📌 메트릭 명세

| 메트릭 이름 | 설명 | 태그 |
|--------------|------|------|
| `sql_query_total` | SQL 실행 횟수 | `query`, `dataSource` |
| `sql_query_duration` | SQL 실행 시간(ms) | `query`, `dataSource` |

---

> 위 메트릭은 MoniKit의 `SqlQueryCountMetricsBinder`, `SqlQueryDurationMetricsBinder`를 통해 자동 수집됩니다.
> Spring Boot + JDBC 환경에서 DB 성능 병목 구간 파악 및 슬로우 쿼리 감지에 유용합니다.
</details>

<details>
<summary><strong>Grafana 쿼리 예시 - HTTP 응답 메트릭 </strong></summary>

# 📊 Grafana 쿼리 예시 - HTTP 응답 메트릭 (`monitoring-metric`)

> 이 문서는 MoniKit에서 수집하는 HTTP 요청/응답 메트릭(`http_response_count`, `http_response_duration`)을 기반으로
> Grafana 대시보드에서 활용할 수 있는 PromQL 쿼리 예시를 제공합니다.

---

## ✅ 응답 횟수 분석

### 1. 전체 HTTP 응답 수 (상태 코드별)
```promql
sum by (status) (http_response_count)
```

### 2. 경로별 HTTP 응답 수
```promql
sum by (path) (http_response_count)
```

### 3. 5xx 에러 비율
```promql
sum(http_response_count{status=~"5.."})
/ sum(http_response_count)
* 100
```

---

## ⏱️ 응답 시간 분석

### 평균 응답 시간 (경로별)
```promql
rate(http_response_duration_sum[5m]) / rate(http_response_duration_count[5m])
```

### p95, p99 응답 시간
```promql
http_response_duration{quantile="0.95"}
```

```promql
http_response_duration{quantile="0.99"}
```

---

## 🔝 느린 엔드포인트 TOP5
```promql
topk(5, rate(http_response_duration_sum[5m]) / rate(http_response_duration_count[5m]))
```

---

## 📌 메트릭 명세

| 메트릭 이름 | 설명 | 태그 |
|--------------------------|--------------------|------------------|
| `http_response_count`    | HTTP 응답 횟수     | `path`, `status` |
| `http_response_duration` | 응답 시간(ms)      | `path`, `status` |

---

> 위 메트릭은 `HttpResponseCountMetricsBinder`, `HttpResponseDurationMetricsBinder`를 통해 자동 수집됩니다.
> Web API 성능 병목 지점 파악, 슬로우 응답 경로 탐색 등에 활용됩니다.

</details>


<details>
<summary><strong> Grafana 쿼리 예시 - 메서드 실행 메트릭 </strong></summary>

# 📊 Grafana 쿼리 예시 - 메서드 실행 메트릭 (`execution_duration`, `execution_count`)

> 이 문서는 MoniKit에서 `ExecutionDetailLog`를 기반으로 수집하는 실행 시간 및 호출 횟수 메트릭을
> Grafana에서 시각화하기 위한 PromQL 쿼리 예시를 제공합니다.

---

## ✅ 실행 횟수

### 1. 전체 메서드 호출 수
```promql
sum by (class, method) (execution_count)
```

### 2. 태그별 호출 수 분석 (예: 외부 API, 배치, 관리자 등)
```promql
sum by (tag) (execution_count)
```

### 3. 특정 클래스 내 메서드별 호출 수
```promql
sum by (method) (execution_count{class="ProductService"})
```

---

## ⏱️ 실행 시간 분석

### 4. 평균 실행 시간 (method 기준)
```promql
rate(execution_duration_sum[5m]) / rate(execution_duration_count[5m])
```

### 5. 특정 태그에 대한 p95 실행 시간
```promql
execution_duration{quantile="0.95", tag="external-api"}
```

### 6. 느린 메서드 TOP5 (평균 실행 시간 기준)
```promql
topk(5, rate(execution_duration_sum[5m]) / rate(execution_duration_count[5m]))
```

---

## 📌 메트릭 명세

| 메트릭 이름         | 설명                  | 태그                     |
|----------------------|-----------------------|--------------------------|
| `execution_count`    | 메서드 호출 횟수      | `class`, `method`, `tag` |
| `execution_duration` | 메서드 실행 시간(ms)  | `class`, `method`, `tag` |

---

> 이 메트릭은 `ExecutionDetailMetricCollector`와 `ExecutionMetricRecorder`를 통해 자동 수집됩니다.
> AOP 기반으로 동작하며, 설정 파일의 `monikit.logging.dynamic-matching` 조건을 만족할 경우만 기록됩니다.
</details>