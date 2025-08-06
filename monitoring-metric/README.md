# 📊 MoniKit Metrics (v1.1.0)

## 📌 개요

`monitoring-metric`은 **Micrometer를 기반으로 한 메트릭 수집 및 Prometheus 통합 모듈**입니다.  
MoniKit의 구조화된 로그 엔트리(`LogEntry`)를 자동으로 메트릭으로 변환하여 **Prometheus**, **Grafana** 등의 모니터링 도구와 완벽하게 연동됩니다.

> ✅ 이 모듈은 **순수 Java + Micrometer**로만 구성되어 있으며,  
> ✅ Spring Boot 프로젝트에서는 `monikit-starter`에 자동 포함됩니다.

---

## ⚡ 핵심 기능

### 🎯 자동 메트릭 수집
- **HTTP 응답 시간**: 경로, 상태 코드별 히스토그램
- **SQL 쿼리 성능**: 쿼리 타입, 실행 시간 분포
- **메서드 실행 시간**: 클래스, 메서드별 성능 지표
- **에러율 추적**: 예외 발생률, 타입별 분류

### 🛡️ 메모리 보호 메커니즘
- **MAX_TIMER_COUNT = 100**: 동적 메트릭 폭발 방지
- **자동 정규화**: URL 파라미터 → `{id}` 패턴 변환
- **캐시 최적화**: ConcurrentHashMap 기반 Timer 재사용

---

## 🏗️ 아키텍처 설계

### 핵심 구성요소

| 컴포넌트 | 역할 |
|----------|------|
| `MetricCollector<T>` | 로그 엔트리별 메트릭 수집 인터페이스 |
| `*MetricsBinder` | Micrometer MeterRegistry 연동 |
| `*MetricsRecorder` | 비즈니스 로직 기반 메트릭 기록 |
| `*MetricUtils` | 공통 메트릭 처리 유틸리티 |

### 메트릭 수집 플로우

```text
[LogEntry 생성] 
    ↓
[MetricCollector.supports() 확인]
    ↓
[MetricCollector.record() 호출]
    ↓
[MetricsBinder를 통해 Micrometer 연동]
    ↓
[Prometheus /metrics 엔드포인트 노출]
```

---

## 📈 지원 메트릭 타입

### 1. HTTP 응답 메트릭

**Counter**: `http_response_count`
```prometheus
http_response_count{path="/api/users",status="200"} 1250
http_response_count{path="/api/users/{id}",status="404"} 23
```

**Timer**: `http_response_duration`
```prometheus
http_response_duration_seconds{path="/api/users",status="200",quantile="0.5"} 0.025
http_response_duration_seconds{path="/api/users",status="200",quantile="0.95"} 0.150
```

### 2. SQL 쿼리 메트릭

**Counter**: `sql_query_count`
```prometheus
sql_query_count{query_type="SELECT",table="users"} 8450
sql_query_count{query_type="INSERT",table="orders"} 234
```

**Timer**: `sql_query_duration`
```prometheus
sql_query_duration_seconds{query_type="SELECT",table="users",quantile="0.99"} 0.045
```

### 3. 메서드 실행 메트릭

**Counter**: `execution_detail_count`
```prometheus
execution_detail_count{class="UserService",method="createUser",tag="user-registration"} 156
```

**Timer**: `execution_detail_duration`
```prometheus
execution_detail_duration_seconds{class="UserService",method="createUser",quantile="0.95"} 0.085
```

---

## 🔧 설정 및 사용법

### 1. 기본 활성화 설정

```yaml
monikit:
  metrics:
    metrics-enabled: true        # 전체 메트릭 수집 활성화
    query-metrics-enabled: true  # SQL 메트릭 수집
    http-metrics-enabled: true   # HTTP 메트릭 수집
```

### 2. 커스텀 메트릭 수집기

```java
@Component
public class CustomMetricCollector implements MetricCollector<ExecutionDetailLog> {
    
    @Override
    public boolean supports(LogType logType) {
        return logType == LogType.EXECUTION_DETAIL;
    }
    
    @Override
    public void record(ExecutionDetailLog logEntry) {
        // 커스텀 메트릭 로직
        Counter.builder("custom_execution_count")
            .tag("service", logEntry.getClassName())
            .register(meterRegistry)
            .increment();
    }
}
```

### 3. 메트릭 노출 확인

```bash
# Prometheus 메트릭 엔드포인트
curl http://localhost:8080/actuator/prometheus | grep monikit

# 특정 메트릭 확인
curl http://localhost:8080/actuator/metrics/http_response_duration
```

---

## 🎯 성능 최적화

### 메트릭 폭발 방지

```java
// HttpResponseDurationMetricsBinder.java
private static final int MAX_TIMER_COUNT = 100;

public void record(String path, int statusCode, long responseTime) {
    String normalizedPath = normalizePath(path); // /api/users/123 → /api/users/{id}
    String key = normalizedPath + "|" + statusCode;
    
    // 🛡️ 메모리 보호: 100개 제한
    if (timerCache.size() >= MAX_TIMER_COUNT && !timerCache.containsKey(key)) {
        return; // 새로운 Timer 생성 차단
    }
    
    // 기존 Timer 재사용 또는 새로 생성
    Timer timer = timerCache.computeIfAbsent(key, this::createTimer);
    timer.record(responseTime, TimeUnit.MILLISECONDS);
}
```

### 경로 정규화

```java
private String normalizePath(String path) {
    if (path == null) return "unknown";
    return path.replaceAll("\\d+", "{id}")        // 숫자 → {id}
               .replaceAll("[a-f0-9-]{36}", "{uuid}"); // UUID → {uuid}
}
```

---

## 📊 Grafana 대시보드 예시

### 1. HTTP 성능 대시보드

```json
{
  "title": "HTTP Response Performance",
  "panels": [
    {
      "title": "Response Time by Endpoint",
      "type": "graph",
      "targets": [
        {
          "expr": "histogram_quantile(0.95, rate(http_response_duration_seconds_bucket[5m]))",
          "legendFormat": "95th percentile - {{path}}"
        }
      ]
    },
    {
      "title": "Request Rate",
      "type": "stat",
      "targets": [
        {
          "expr": "rate(http_response_count[5m])",
          "legendFormat": "{{path}} - {{status}}"
        }
      ]
    }
  ]
}
```

### 2. 비즈니스 메트릭 대시보드

```json
{
  "title": "Business Metrics",
  "panels": [
    {
      "title": "User Registration Rate",
      "type": "graph",
      "targets": [
        {
          "expr": "rate(execution_detail_count{tag=\"user-registration\"}[5m])",
          "legendFormat": "Registrations/sec"
        }
      ]
    }
  ]
}
```

---

## 🔍 트러블슈팅

### 1. 메트릭이 노출되지 않는 경우

```yaml
# 설정 확인
monikit.metrics.metrics-enabled: true

# Actuator 엔드포인트 활성화
management:
  endpoints:
    web:
      exposure:
        include: prometheus,metrics
```

### 2. 메트릭 수가 급격히 증가하는 경우

```bash
# 현재 메트릭 수 확인
curl -s http://localhost:8080/actuator/prometheus | wc -l

# Timer 개수 확인 (로그에서)
grep "Timer cache size" application.log
```

**해결책**:
- `normalizePath()` 로직 개선
- `excluded-paths` 설정으로 불필요한 경로 제외

### 3. 성능 영향 최소화

```java
// 샘플링 기반 메트릭 수집
@ConditionalOnProperty(name = "monikit.metrics.sampling-rate", havingValue = "0.1")
public MetricCollector samplingMetricCollector() {
    return new SamplingMetricCollector(0.1); // 10% 샘플링
}
```

---

## 🔗 연동 모듈

| 모듈 | 연동 방식 |
|------|----------|
| `monitoring-core` | LogEntry → MetricCollector 자동 변환 |
| `monitoring-starter` | Spring Boot AutoConfiguration |
| `monitoring-starter-web` | HTTP 메트릭 자동 수집 |
| **Micrometer** | MeterRegistry 기반 메트릭 등록 |
| **Prometheus** | `/actuator/prometheus` 엔드포인트 |

---

## 📝 모범 사례

### 1. 메트릭 명명 규칙
```java
// ✅ 좋은 예
Counter.builder("user_registration_count")
    .tag("source", "web")
    .tag("status", "success")
    .register(meterRegistry);

// ❌ 나쁜 예
Counter.builder("count")  // 너무 일반적
    .tag("user_id", userId)  // 고유값은 태그로 사용 금지
    .register(meterRegistry);
```

### 2. 태그 사용법
```java
// ✅ 카디널리티가 낮은 태그 사용
.tag("http_method", "GET")      // 7개 정도
.tag("status_class", "2xx")     // 5개 정도
.tag("service", "user-service") // 서비스 수

// ❌ 카디널리티가 높은 태그 사용 금지
.tag("user_id", userId)         // 수백만 개
.tag("timestamp", timestamp)    // 무한대
```

---

(c) 2024 Ryu-qqq. MoniKit 프로젝트