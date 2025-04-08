
# MoniKit Starter WEB

## 📌 개요

`monikit-starter-web`은 Spring 웹 애플리케이션에서 **HTTP 요청을 자동 추적하고**, **로그 및 메트릭 수집**을 지원하는 경량 로깅 모듈입니다.  
클라이언트 요청부터 응답까지의 흐름을 **Trace ID 기반으로 추적**하고, **슬로우 응답 감지**, **요청 본문 및 응답 본문 기록**, **예외 자동 로깅** 기능을 제공합니다.

---

## ⚙️ 기본 기능

- `TraceIdFilter`: Trace ID 자동 생성 및 MDC에 설정
- `LogContextScopeFilter`: 요청 단위 로깅 컨텍스트 생성 및 정리
- `HttpLoggingInterceptor`: 요청/응답 정보 로깅
- 요청/응답 본문 캡처 (`RequestWrapper`, `ContentCachingResponseWrapper`)
- 메트릭 수집 및 커스텀 로깅 인터페이스 연동
- 최소한의 설정으로 자동 구성 지원

---

## 🧩 주요 흐름

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

## 🧱 주요 구성 요소

### 1. `TraceIdFilter`
- 요청에 `X-Trace-Id` 헤더가 없으면 UUID 생성
- `TraceIdProvider`를 통해 Trace ID를 MDC에 저장
- 응답에도 동일한 `X-Trace-Id` 포함

### 2. `LogContextScopeFilter`
- 요청 시작 시 MDC Scope를 열고, 종료 시 자동 정리
- 요청/응답 본문 캡처 가능 (`RequestWrapper`, `ContentCachingResponseWrapper` 사용)
- 제외 경로(`EXCLUDED_PATHS`) 처리 지원

### 3. `HttpLoggingInterceptor`
- `HandlerInterceptor` 구현체
- 요청 로그: 메서드, URI, 쿼리, 헤더, 바디, IP, User-Agent 등
- 응답 로그: 상태코드, 헤더, 바디, 처리 시간
- `TraceIdProvider` 및 `LogEntryContextManager` 사용

---

## ⚙️ 자동 설정 구성

### 1. `FilterAutoConfiguration`
- `TraceIdFilter`, `LogContextScopeFilter` 자동 등록
- 순서 지정 (`TraceIdFilter`: 1번, `LogContextScopeFilter`: 2번)
- 조건부 등록 (`monikit.logging.filters.trace-enabled`, `log-enabled`)

### 2. `HttpLoggingInterceptorConfiguration`
- `HttpLoggingInterceptor` 빈 자동 등록
- `log-enabled`이 `true`일 때만 활성화

### 3. `InterceptorAutoConfiguration`
- `WebMvcConfigurer`를 통해 `HttpLoggingInterceptor` 등록
- `log-enabled=false`일 경우 등록 안함

---

## 📄 관련 설정 (application.yml)

```yaml
monikit:
  logging:
    log-enabled: true
    trace-enabled: true
    datasource-logging-enabled: false
    filters:
      trace-enabled: true
      log-enabled: true
```

---

## 📌 참고할 클래스

| 클래스명 | 설명 |
|----------|------|
| `TraceIdFilter` | Trace ID 설정 및 응답 포함 필터 |
| `LogContextScopeFilter` | 요청 범위 MDC 관리 필터 |
| `HttpLoggingInterceptor` | 요청 및 응답 자동 로깅 |
| `FilterAutoConfiguration` | 필터 자동 설정 클래스 |
| `HttpLoggingInterceptorConfiguration` | 인터셉터 빈 등록 |
| `InterceptorAutoConfiguration` | WebMvc에 인터셉터 등록 |

---

## 🧪 테스트 팁

- 필터/인터셉터 비활성화 테스트: `log-enabled=false` 설정
- 요청 본문, 응답 본문 로그 확인: `ContentCachingResponseWrapper` 로그 확인
- `EXCLUDED_PATHS` 정의로 특정 URI 로그 제외 가능

