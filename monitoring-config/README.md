# MoniKit Config 모듈

## 개요

`monikit-config`는 MoniKit의 설정 구성 요소를 담고 있는 순수 Java 모듈입니다.  
이 모듈은 어떤 실행 환경(Spring 포함)에도 의존하지 않으며,  
다른 `starter` 모듈들이 `@EnableConfigurationProperties` 또는 `@AutoConfiguration`을 통해  
필요한 설정 객체를 바인딩하도록 구성됩니다.

---

## 포함된 설정 클래스

### `MoniKitLoggingProperties`

```yaml
monikit.logging:
  log-enabled: true
  trace-enabled: true
  detailed-logging: false
  summary-logging: true
  threshold-millis: 300
  slow-query-threshold-ms: 1000
  critical-query-threshold-ms: 5000
  datasource-logging-enabled: true
```

- `logEnabled`: 전체 로깅 기능 마스터 스위치
- `traceEnabled`: traceId 기반 추적 로그 활성화
- `detailedLogging`: SQL 파라미터, input/output 로깅 여부
- `summaryLogging`: Execution 로그 요약 여부 (기본 true)
- `thresholdMillis`: 상세 로그로 전환될 기준 시간 (ms)
- `slowQueryThresholdMs`: 느린 쿼리 기준
- `criticalQueryThresholdMs`: 매우 느린 쿼리 기준
- `datasourceLoggingEnabled`: JDBC 쿼리 로깅 활성화

---

### `MoniKitMetricsProperties`

```yaml
monikit.metrics:
  metrics-enabled: true
  query-metrics-enabled: true
  http-metrics-enabled: false
  slow-query-threshold-ms: 2000
  query-sampling-rate: 10
```

- `metricsEnabled`: 전체 메트릭 수집 마스터 스위치
- `queryMetricsEnabled`: SQL 쿼리 메트릭 수집 여부
- `httpMetricsEnabled`: HTTP 요청 메트릭 수집 여부
- `slowQueryThresholdMs`: 느린 쿼리 기준 (ms)
- `querySamplingRate`: 샘플링 비율 (%) — 100이면 전부 수집

---

## 주의사항

- 이 모듈은 어떤 Spring 구성도 자동 등록하지 않습니다.
- 바인딩 및 빈 등록은 반드시 `monikit-starter-*`에서 수행해야 합니다.
- 테스트나 다른 모듈에서도 설정 객체를 재사용할 수 있도록 **순수 Java 클래스만 포함**됩니다.

---

## 사용 예시 (Spring Starter)

```java
@AutoConfiguration
@EnableConfigurationProperties(MoniKitLoggingProperties.class)
public class MoniKitLoggingAutoConfiguration {
}
```