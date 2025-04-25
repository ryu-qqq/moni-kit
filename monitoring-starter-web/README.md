# MoniKit Starter WEB (v1.1.3)

## 📌 개요

`monikit-starter-web`은 Spring 웹 애플리케이션에서 **HTTP 요청/응답을 자동 추적하고**,  
**로그 및 메트릭 수집**, **Trace ID 기반 요청 흐름 추적**, **예외 자동 로깅**을 지원하는 경량 로깅 모듈입니다.

> ✅ 이 모듈은 `monikit-starter`를 내부적으로 포함하고 있어,  
> ✅ 의존성 하나만 추가하면 JDBC 감시, 메트릭 수집, 로깅 설정까지 전부 자동으로 적용됩니다.

---

## ⚙️ 포함 기능

- Trace ID 기반 요청 흐름 추적 (`X-Trace-Id`)
- 슬로우 응답 감지 및 구조화 로그 수집
- HTTP 요청/응답 본문 캡처 및 로깅
- Micrometer 기반 응답 시간/횟수 메트릭 수집
- MDC 기반 컨텍스트 전파 및 범위 관리
- AOP 기반 메서드 실행 시간 로깅 (Core 포함)
- JDBC 쿼리 감시 자동 적용 (조건부)

---

## ✅ 자동 포함 구성

의존성 하나로 아래 모듈이 자동으로 포함됩니다:

```
monikit-starter-web
├── monitoring-starter
│   ├── monitoring-core
│   ├── monitoring-config
│   ├── monitoring-metric
│   ├── monitoring-jdbc
│   └── monitoring-slf4j
└── web-specific filter/interceptor 설정
```

---

## 🧩 요청 처리 흐름

```text
[클라이언트 요청]
    |
    ▼
[TraceIdFilter] → Trace ID 설정 및 응답 헤더 포함
    |
    ▼
[LogContextScopeFilter] → 요청 범위 MDC 생성 및 종료
    |
    ▼
[HttpLoggingInterceptor] → 요청/응답 로깅
    |
    ▼
[LogEntryContextManager] → 로그 수집 처리
```

---

## ⚙️ 자동 설정 클래스

| 클래스명 | 설명 |
|----------|------|
| `FilterAutoConfiguration` | TraceId, Scope 필터 자동 등록 |
| `HttpLoggingInterceptorConfiguration` | Interceptor Bean 등록 |
| `InterceptorAutoConfiguration` | Spring WebMvc에 인터셉터 적용 |
| (포함) `DataSourceLoggingConfig` | JDBC 감시 자동 적용 (`log-enabled`, `datasource-logging-enabled` 조건) |
| (포함) `MoniKitMeterBinderAutoConfiguration` | Micrometer 연동 메트릭 자동 등록 |

---

## 🔧 설정 예시 (application.yml)

```yaml
monikit:
  logging:
    log-enabled: true
    datasource-logging-enabled: true
    dynamic-matching:
      - classNamePattern: ".*Service"
        methodNamePattern: ".*Create"
        when: "#executionTime > 300"
        thresholdMillis: 300
        tag: "slow-service"


  web:
    excluded-paths:
      - /actuator/health
      - /actuator/prometheus
      - /actuator/metrics
      - /metrics
      - /health
      - /
```

> `monikit.web.excluded-paths`를 통해 로그 수집에서 제외할 경로를 직접 정의할 수 있습니다.

---

## 💡 사용자 확장성

- `LogSink`, `TraceIdProvider`, `MetricCollector` 등은 `@ConditionalOnMissingBean`으로 선언되어 있어
  직접 구현체를 주입하면 자동으로 교체됩니다.
- 설정 기반만으로 대부분의 기능이 동작하며, 필터/인터셉터 비활성화도 용이합니다.
- `excluded-paths`는 YAML 설정으로 손쉽게 조절할 수 있습니다.

---

## 🧪 테스트 팁

- 필터/인터셉터 비활성화 테스트: `log-enabled=false`
- 요청 본문, 응답 본문 확인: `ContentCachingResponseWrapper` 로그 확인
- `excluded-paths` 설정을 변경해 특정 경로 로깅 제외 가능

---

(c) 2025 Ryu Sangwon. MoniKit 프로젝트
