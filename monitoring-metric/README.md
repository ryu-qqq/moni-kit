# MoniKit Metric

## 개요
이 문서는 `monikit.metric` 패키지의 핵심 설정을 설명합니다.

---
## **1. 메트릭 자동 등록** (`MetricCollectorAutoConfiguration`)

`MetricCollectorAutoConfiguration` 클래스는 **특정 메트릭 수집이 활성화된 경우, 자동으로 적절한 `MetricCollector` 빈을 등록**합니다.

### ✅ 주요 메트릭 자동 등록
| 등록 대상 | 역할 | 활성화 설정 |
|--------|------|------------|
| `DatabaseQueryMetricCollector` | SQL 쿼리 실행 시간 및 총 실행 횟수를 기록 | `monikit.metrics.queryMetricsEnabled=true` |
| `HttpInboundResponseMetricCollector` | HTTP 요청 응답 시간 및 상태 코드별 요청 수를 기록 | `monikit.metrics.httpMetricsEnabled=true` |
| `HttpOutboundResponseMetricCollector` | 외부 API 호출 응답 시간 및 응답 코드 기록 | `monikit.metrics.externalMallMetricsEnabled=true` |

### 🔧 **설정 옵션**
| 옵션명 | 기본값 | 설명 |
|--------|--------|------|
| `monikit.metrics.queryMetricsEnabled` | `true` | SQL 쿼리 메트릭 활성화 여부 |
| `monikit.metrics.httpMetricsEnabled` | `true` | HTTP 응답 메트릭 활성화 여부 |

### **설정 예시 (application.yml)**
```yaml
monikit:
  metrics:
    queryMetricsEnabled: true
    httpMetricsEnabled: true
```

---


## **2. 요약**
| 설정 클래스 | 역할 | 관련 설정 prefix |
|------------|------|----------------|
| `DataSourceLoggingConfig` | 데이터소스 로깅 설정 자동 적용 | `monikit.logging` |
| `ErrorLogNotifierAutoConfiguration` | 에러 로그 감지 및 처리 | 자동 빈 등록 |
| `FilterAutoConfiguration` | HTTP 요청 관련 필터 자동 등록 | `monikit.logging.filters`, `monikit.metrics` |

🚀 **MoniKit Starter의 자동 설정 기능을 활용하면 복잡한 설정 없이 손쉽게 강력한 로깅 및 메트릭 기능을 사용할 수 있습니다!**

