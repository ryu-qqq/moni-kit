# MoniKit Starter

## 개요
MoniKit Starter는 **Spring Boot 환경에서 MoniKit의 로깅 및 메트릭 수집 기능을 쉽게 설정할 수 있도록 지원하는 자동 설정 모듈**입니다.  
이 모듈을 사용하면 `application.yml` 또는 `application.properties`에서 간단한 설정만으로 **SQL 로깅, HTTP 요청 로깅, Trace ID 관리, 메트릭 수집** 등을 활성화할 수 있습니다.

이 문서는 `monikit.starter` 패키지의 핵심 설정을 설명합니다.

---

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

## **2. 메트릭 수집 설정**
### MoniKitMetricsProperties (`monikit.metrics`)

`MoniKitMetricsProperties` 클래스는 **HTTP 및 SQL 쿼리 실행 시간 등의 메트릭을 수집할지 여부를 설정**합니다.

```java
@ConfigurationProperties(prefix = "monikit.metrics")
public class MoniKitMetricsProperties {
    private boolean metricsEnabled = true;
}
```

### **설정 옵션**
| 옵션명 | 기본값 | 설명 |
|--------|--------|------|
| `monikit.metrics.metricsEnabled` | `true` | HTTP 요청 및 SQL 쿼리 실행 메트릭 수집 활성화 여부 |

### **설정 예시 (application.yml)**
```yaml
monikit:
  metrics:
    metricsEnabled: false
```

---

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

## **4. MoniKit Starter 사용법**
### **Spring Boot 프로젝트에 적용하기**
1. `monikit-starter` 의존성 추가 (Gradle)
```gradle
dependencies {
    implementation 'com.monikit:monikit-starter:1.0.0'
}
```

2. **설정 파일 (`application.yml`)에서 로깅 및 메트릭 설정**
```yaml
monikit:
  logging:
    logEnabled: true
    traceEnabled: true
    detailedLogging: false
    slowQueryThresholdMs: 1500
    criticalQueryThresholdMs: 5000
  metrics:
    metricsEnabled: true
```

3. **Spring Boot 실행 시 자동으로 설정이 반영됨**
    - SQL 로깅, Trace ID 추적, 메트릭 수집 기능이 활성화됨.

---

## **5.MoniKit Starter - Configuration

## 개요
MoniKit Starter의 `config` 패키지는 **Spring Boot에서 MoniKit의 로깅 및 메트릭 기능을 자동으로 설정하고 관리하는 역할**을 합니다.  
이 모듈을 통해 **SQL 로깅, HTTP 요청 로깅, Trace ID 관리, 필터 등록** 등의 설정을 간단하게 활성화할 수 있습니다.

---

## **1. 데이터소스 로깅 설정** (`DataSourceLoggingConfig`)

`DataSourceLoggingConfig`는 **데이터소스(SQL 로깅) 관련 설정을 자동으로 적용**하는 클래스입니다.

### ✅ 주요 기능
- **SQL 실행 로깅을 위한 `LoggingDataSource` 적용**
- `logEnabled=true` && `datasourceLoggingEnabled=true`일 경우 `LoggingPreparedStatementFactory`를 활용하여 SQL 실행을 로깅
- `DataSourceProvider`가 빈으로 등록되지 않으면 `DefaultDataSourceProvider` 자동 사용

### 🔧 **설정 옵션**
| 옵션명 | 기본값 | 설명 |
|--------|--------|------|
| `monikit.logging.logEnabled` | `true` | MoniKit 로깅 전체 활성화 여부 |
| `monikit.logging.datasourceLoggingEnabled` | `true` | 데이터베이스 로깅 활성화 여부 |

### **설정 예시 (application.yml)**
```yaml
monikit:
  logging:
    logEnabled: true
    datasourceLoggingEnabled: true
```

---

## **2. 에러 로그 감지 설정** (`ErrorLogNotifierAutoConfiguration`)

`ErrorLogNotifierAutoConfiguration`은 **애플리케이션 내에서 발생한 에러 로그를 감지하고 처리하는 기능**을 제공합니다.

### ✅ 주요 기능
- 사용자가 `ErrorLogNotifier` 빈을 직접 등록하면 해당 빈을 사용
- 별도의 빈이 없을 경우 기본적으로 `DefaultErrorLogNotifier`를 주입

### **설정 예시**
사용자가 별도 `ErrorLogNotifier` 구현체를 제공하지 않으면 자동으로 `DefaultErrorLogNotifier`가 등록됩니다.

```java
@Bean
@ConditionalOnMissingBean(ErrorLogNotifier.class)
public ErrorLogNotifier defaultErrorLogNotifier() {
    return DefaultErrorLogNotifier.getInstance();
}
```

---

## **3. 필터 자동 등록** (`FilterAutoConfiguration`)

`FilterAutoConfiguration`은 **HTTP 요청 로깅 및 메트릭 수집을 위한 필터를 자동으로 등록**하는 역할을 합니다.

### ✅ 주요 필터
| 필터명 | 역할 | 활성화 설정 |
|--------|------|------------|
| `TraceIdFilter` | HTTP 요청마다 Trace ID를 설정하여 로깅 | `monikit.logging.filters.trace-enabled=true` |
| `LogContextScopeFilter` | 요청 단위로 로그 컨텍스트를 관리 | `monikit.logging.filters.log-enabled=true` |
| `HttpMetricsFilter` | HTTP 요청에 대한 실행 시간 메트릭을 수집 | `monikit.logging.filters.metrics-enabled=true` |

### 🔧 **설정 옵션**
| 옵션명 | 기본값 | 설명 |
|--------|--------|------|
| `monikit.logging.filters.trace-enabled` | `true` | Trace ID 필터 활성화 여부 |
| `monikit.logging.filters.log-enabled` | `true` | 로그 컨텍스트 필터 활성화 여부 |
| `monikit.logging.filters.metrics-enabled` | `true` | HTTP 메트릭 필터 활성화 여부 |

### **설정 예시 (application.yml)**
```yaml
monikit:
  logging:
    filters:
      trace-enabled: true
      log-enabled: true
  metrics:
    metrics-enabled: true
```

---

## **4. 요약**
| 설정 클래스 | 역할 | 관련 설정 prefix |
|------------|------|----------------|
| `DataSourceLoggingConfig` | 데이터소스 로깅 설정 자동 적용 | `monikit.logging` |
| `ErrorLogNotifierAutoConfiguration` | 에러 로그 감지 및 처리 | 자동 빈 등록 |
| `FilterAutoConfiguration` | HTTP 요청 관련 필터 자동 등록 | `monikit.logging.filters`, `monikit.metrics` |

🚀 **MoniKit Starter의 자동 설정 기능을 활용하면 복잡한 설정 없이 손쉽게 강력한 로깅 및 메트릭 기능을 사용할 수 있습니다!**

