# MoniKit Starter

## 📌 프로젝트 개요
**MoniKit Starter**는 서버의 로깅 및 모니터링을 자동으로 설정하는 Spring Boot Starter 라이브러리입니다.
개발자가 별도의 설정 없이도 데이터베이스, HTTP 요청, 메트릭 수집 등을 쉽게 모니터링할 수 있도록 합니다.

## 🔧 주요 기능

### 1. **데이터베이스 로깅**
- `LoggingDataSource`를 통해 SQL 실행 로그를 자동으로 기록
- `DefaultDataSourceProvider`가 기본적으로 제공됨
- 쿼리 실행 시간 및 성능 메트릭 자동 수집

### 2. **에러 로깅 자동화**
- `ErrorLogNotifier` 자동 주입
- 별도의 설정이 없을 경우 `DefaultErrorLogNotifier`를 사용

### 3. **HTTP 필터 및 인터셉터 자동 적용**
- `TraceIdFilter`: 모든 요청에 고유한 Trace ID 부여
- `LogContextScopeFilter`: 로그 컨텍스트 관리
- `HttpMetricsFilter`: HTTP 요청의 메트릭 자동 수집
- `HttpLoggingInterceptor`: HTTP 요청/응답 로깅

### 4. **로깅 컨텍스트 관리**
- `LogEntryContextManager` 자동 주입
- `ThreadContextHandler`를 활용한 멀티스레드 컨텍스트 전파 지원

### 5. **메트릭 수집 기능**
- `PrometheusMetricCollector` 지원 (Micrometer 연동 가능)
- `monikit.metrics.enabled=false` 설정 시 메트릭 비활성화

### 6. **쿼리 실행 로그 및 성능 모니터링**
- `QueryLoggingService`를 통해 SQL 실행 로깅 및 메트릭 수집
- `slowQueryThresholdMs`, `criticalQueryThresholdMs` 설정 가능

## ⚙️ 설정 방법

### 1. **Gradle 추가**
```gradle
implementation 'com.monikit:monikit-starter:1.0.1'
```

### 2. **설정 프로퍼티 (application.yml)**
```yaml
monikit:
  logging:
    detailedLogging: true
    slowQueryThresholdMs: 1000
    criticalQueryThresholdMs: 5000
    datasourceLoggingEnabled: true
    filtersEnabled: true
    traceEnabled: true
    interceptorsEnabled: true
  metrics:
    enabled: true
```

## 🛠 주요 설정 클래스
| 클래스명 | 설명 |
|----------|---------------------------------|
| `DataSourceLoggingConfig` | 데이터소스 로깅 자동 설정 |
| `ErrorLogNotifierAutoConfiguration` | 에러 로깅 설정 |
| `FilterAutoConfiguration` | HTTP 필터 자동 적용 |
| `InterceptorAutoConfiguration` | HTTP 인터셉터 자동 적용 |
| `LogEntryContextManagerConfig` | 로깅 컨텍스트 및 에러 노티파이어 설정 |
| `LogNotifierAutoConfiguration` | 로그 노티파이어 자동 설정 (Logback 지원) |
| `MetricCollectorAutoConfiguration` | 메트릭 수집 설정 (Prometheus 지원) |
| `QueryLoggingConfig` | SQL 실행 로그 및 메트릭 수집 |

## 🔍 커스텀 설정 예시
### 1. **커스텀 `LogNotifier` 등록**
```java
@Bean
public LogNotifier customLogNotifier() {
    return new CustomLogNotifier();
}
```

### 2. **쿼리 실행 임계값 변경**
```yaml
monikit:
  logging:
    slowQueryThresholdMs: 500
    criticalQueryThresholdMs: 2000
```


