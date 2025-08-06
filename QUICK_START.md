# 🚀 MoniKit OpenTelemetry Quick Start

**한 줄 설정으로 OpenTelemetry 업그레이드!**

## ⚡ 30초 설정

### 1. 의존성 추가
```gradle
dependencies {
    implementation 'com.monikit:monikit-starter-web:1.1.3'  // 기존 유지
    implementation 'com.monikit:monikit-otel:2.0.0'        // 🔥 추가
}
```

### 2. 설정 한 줄 추가
```yaml
monikit:
  otel:
    enabled: true  # 🔥 이것만 추가!
```

## 🎯 자동 전환 메커니즘

### Before (기존)
```yaml
# application.yml
monikit:
  logging:
    log-enabled: true
```
→ **결과**: `ExecutionLoggingAspect` 사용 (기존 방식)

### After (OpenTelemetry)
```yaml
# application.yml  
monikit:
  otel:
    enabled: true  # 🔥 한 줄 추가
  logging:
    log-enabled: true  # 기존 설정 유지 (선택사항)
```
→ **결과**: `OtelExecutionLoggingAspect` 사용 (OpenTelemetry 방식)

## 🔄 OpenTelemetry 우선 접근

### 기존 moni-kit vs OpenTelemetry

| 기능 | 기존 moni-kit | OpenTelemetry |
|------|---------------|---------------|
| **실행 시간 측정** | 커스텀 로그 | Span duration (자동) |
| **메트릭 수집** | 커스텀 Hook | OpenTelemetry Metrics |
| **분산 추적** | ThreadLocal TraceId | 표준 TraceId/SpanId |
| **로그 상관관계** | MDC 수동 주입 | 자동 주입 |
| **AWS 연동** | ELK + Prometheus | CloudWatch + X-Ray (네이티브) |

### 🎉 OpenTelemetry의 장점

1. **표준 준수**: 업계 표준 관측성 프레임워크
2. **AWS 네이티브**: CloudWatch, X-Ray 직접 연동
3. **성능 최적화**: 중복 처리 없음, 효율적인 배치 전송
4. **자동 상관관계**: TraceId, SpanId 자동 연결
5. **풍부한 메타데이터**: 표준 Semantic Conventions

## 📊 실제 동작 예시

### OpenTelemetry Span 속성
```json
{
  "traceId": "1234567890abcdef",
  "spanId": "abcdef1234567890",
  "operationName": "UserService.createUser",
  "attributes": {
    "code.function": "createUser",
    "code.namespace": "com.example.UserService",
    "monikit.execution.time_ms": 150,
    "monikit.execution.threshold_ms": 100,
    "monikit.tag": "user-creation",
    "monikit.slow_execution": true,
    "monikit.arguments": "[{\"name\":\"john\"}]",
    "monikit.result": "{\"id\":123,\"name\":\"john\"}"
  }
}
```

## 🔧 고급 설정

### OTLP 엔드포인트 설정
```yaml
monikit:
  otel:
    enabled: true
    otlp:
      traces-endpoint: "http://localhost:4318/v1/traces"
      metrics-endpoint: "http://localhost:4318/v1/metrics"
      logs-endpoint: "http://localhost:4318/v1/logs"
```

### AWS ADOT Collector 연동
```yaml
monikit:
  otel:
    enabled: true
    otlp:
      traces-endpoint: "http://adot-collector:4318/v1/traces"
      metrics-endpoint: "http://adot-collector:4318/v1/metrics"
```

## 🚀 마이그레이션 완료!

이제 애플리케이션을 시작하면:

```
🚀 OpenTelemetry ExecutionLoggingAspect activated - pure OpenTelemetry mode
📊 Metrics, tracing, and logs will be handled by OpenTelemetry standard
```

**기존 SpEL 규칙, 동적 매칭은 그대로 작동하면서 OpenTelemetry의 강력함을 경험하세요!** ✨
