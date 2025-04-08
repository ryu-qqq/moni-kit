
# Monitoring Config

## **1. 로깅 설정**
### MoniKitLoggingProperties (`monikit.logging`)

`MoniKitLoggingProperties` 클래스는 **MoniKit의 로깅 관련 설정을 관리**하는 역할을 합니다.

```java
@ConfigurationProperties(prefix = "monikit.logging")
public class MoniKitLoggingProperties {
    private boolean detailedLogging = false;
    private long slowQueryThresholdMs = 1000;
    private long criticalQueryThresholdMs = 5000;
    private boolean datasourceLoggingEnabled = true;
    private boolean traceEnabled = true;
    private boolean logEnabled = true;
}
```

### **설정 옵션**
| 옵션명 | 기본값 | 설명 |
|--------|--------|------|
| `monikit.logging.detailedLogging` | `false` | 세부 로그를 활성화할지 여부 |
| `monikit.logging.slowQueryThresholdMs` | `1000ms` | SQL 실행 시간이 이 값보다 크면 WARN 로그로 기록 |
| `monikit.logging.criticalQueryThresholdMs` | `5000ms` | SQL 실행 시간이 이 값보다 크면 ERROR 로그로 기록 |
| `monikit.logging.datasourceLoggingEnabled` | `true` | 데이터베이스 쿼리 로깅 활성화 여부 |
| `monikit.logging.traceEnabled` | `true` | Trace ID 로깅 활성화 여부 |
| `monikit.logging.logEnabled` | `true` | MoniKit 로깅 전체 활성화 여부 |

### **설정 예시 (application.yml)**
```yaml
monikit:
  logging:
    detailedLogging: true
    slowQueryThresholdMs: 2000
    criticalQueryThresholdMs: 7000
    datasourceLoggingEnabled: false
    traceEnabled: true
    logEnabled: true
```

---

## 2. 메트릭 수집 설정
### MoniKitMetricsProperties (`monikit.metrics`)

`MoniKitMetricsProperties` 클래스는 **HTTP 요청, SQL 쿼리 실행 시간, 외부몰 요청 메트릭 등을 수집할지 여부를 설정**합니다.

```java
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {
    private boolean metricsEnabled = true;
    private boolean queryMetricsEnabled = true;
    private boolean httpMetricsEnabled = true;
    private boolean externalMallMetricsEnabled = true;
    private long slowQueryThresholdMs = 2000;
    private int querySamplingRate = 10;

}
```

### **설정 옵션**
| 옵션명 | 기본값 | 설명 |
|--------|--------|------|
| `monikit.metrics.metricsEnabled` | `true` | 전체 메트릭 수집 활성화 여부 |
| `monikit.metrics.queryMetricsEnabled` | `true` | SQL 쿼리 실행 메트릭 수집 활성화 여부 |
| `monikit.metrics.httpMetricsEnabled` | `true` | HTTP 요청 메트릭 수집 활성화 여부 |
| `monikit.metrics.externalMallMetricsEnabled` | `true` | 외부몰 요청 메트릭 수집 활성화 여부 |
| `monikit.metrics.slowQueryThresholdMs` | `2000` | 슬로우 쿼리 감지 임계값 (ms) |
| `monikit.metrics.querySamplingRate` | `10` | SQL 쿼리 샘플링 비율 (%) |

### **설정 예시 (application.yml)**
```yaml
monikit:
  metrics:
    metricsEnabled: true
    queryMetricsEnabled: true
    httpMetricsEnabled: true
    externalMallMetricsEnabled: true
    slowQueryThresholdMs: 3000
    querySamplingRate: 20
```

## **3. 설정 유효성 검사**
### 🚨 `logEnabled`가 `false`일 때 개별 로깅 옵션이 활성화되어 있으면 경고 발생

MoniKit Starter는 **잘못된 설정을 방지하기 위해 자동으로 유효성을 검사**합니다.  
만약 **`logEnabled`가 `false`인데 개별 로깅 옵션이 `true`이면** 경고 로그가 출력됩니다.

```java
@PostConstruct
public void validateLoggingConfiguration() {
    if (!logEnabled && (datasourceLoggingEnabled || traceEnabled || detailedLogging)) {
        logger.warn("logEnabled is disabled (false), but some logging settings (datasourceLoggingEnabled, traceEnabled, detailedLogging) are enabled. Logging may not be recorded.");
    }
}
```

### **예제: 잘못된 설정 (경고 발생)**
```yaml
monikit:
  logging:
    logEnabled: false
    datasourceLoggingEnabled: true
```
➡ **경고 출력**: `"logEnabled is false, but datasourceLoggingEnabled is enabled. Logging may not be recorded."`

---
