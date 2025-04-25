# 📦 MoniKit Config (v1.1.2)

`monikit-config`는 **MoniKit의 핵심 설정값**을 정의하는 **순수 Java 기반 모듈**입니다.  
Spring Boot 외부 환경에 의존하지 않으며, Starter 모듈에서 바인딩하여 사용됩니다.

---

## ⚙️ 지원 설정 목록

### 1. `MoniKitLoggingProperties`

```yaml
monikit.logging:
  log-enabled: true
  datasource-logging-enabled: true
  slow-query-threshold-ms: 1000
  critical-query-threshold-ms: 5000
  allowed-packages:
    - "com.ryuqq"
    - "com.monikit"
  dynamic-matching:
    - classNamePattern: "^External.*Client"
      methodNamePattern: ".*"
      when: "#executionTime > 1000"
      thresholdMillis: 1000
      tag: "external-api"
    - classNamePattern: ".*ProductService"
      methodNamePattern: ".*Register"
      when: "#executionTime > 200"
      thresholdMillis: 200
      tag: "product-registration"
```

| 설정 항목 | 설명 |
|------------|------|
| `log-enabled` | 전체 로깅 기능 ON/OFF |
| `datasource-logging-enabled` | JDBC SQL 로그 출력 여부 |
| `slow-query-threshold-ms` | 느린 쿼리 기준 시간 (ms) |
| `critical-query-threshold-ms` | 매우 느린 쿼리 기준 시간 (ms) |
| `allowed-packages` | 로깅 대상 패키지 제한 (로깅 필터 1차 조건) |
| `dynamic-matching` | 클래스명/메서드명 + 조건식 기반 로깅 필터링 |

---

### 🔍 Dynamic Matching 설명

| 항목 | 설명 |
|------|------|
| `classNamePattern` | 정규식 기반 클래스명 필터 |
| `methodNamePattern` | 정규식 기반 메서드명 필터 |
| `when` | SpEL 조건식. 실행 시간, 메서드명, 인자 등을 기준으로 조건 평가 |
| `thresholdMillis` | 로그에 기록되는 기준 실행시간 |
| `tag` | 로그를 분류할 수 있는 태그 (예: `"external-api"`) |

#### SpEL 지원 변수

- `#executionTime`: 메서드 실행 시간 (ms)
- `#methodName`: 현재 메서드명
- `#className`: 현재 클래스명
- `#args`: 메서드 인자 배열

#### 예시 조건

| 조건 | 의미 |
|------|------|
| `#executionTime > 300` | 300ms 초과 메서드만 로깅 |
| `#methodName.startsWith('sync')` | `sync`로 시작하는 메서드만 |
| `#className.contains('Batch')` | Batch 관련 클래스만 |

---

### 2. `MoniKitMetricsProperties`

```yaml
monikit.metrics:
  metrics-enabled: true
  query-metrics-enabled: true
  http-metrics-enabled: false
```

| 설정 항목 | 설명 |
|------------|------|
| `metrics-enabled` | 전체 메트릭 수집 ON/OFF |
| `query-metrics-enabled` | SQL 쿼리 메트릭 수집 여부 |
| `http-metrics-enabled` | HTTP 요청 메트릭 수집 여부 |

---

---

## 💡 참고
- 이 모듈은 설정 객체만 포함되며, **빈 등록은 하지 않습니다.**
- `monikit-starter-*` 모듈에서 설정 클래스를 가져와 사용합니다.