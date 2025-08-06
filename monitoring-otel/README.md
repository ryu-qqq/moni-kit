# 🚀 MoniKit OpenTelemetry Integration (v2.0.0)

## 📌 개요

`monitoring-otel`은 **OpenTelemetry 표준**을 기반으로 한 차세대 관측성 모듈입니다.  
기존 MoniKit의 커스텀 로깅 시스템을 **업계 표준 OpenTelemetry**로 완전히 마이그레이션하여  
**AWS X-Ray**, **CloudWatch**, **Managed Grafana**와 네이티브 통합을 제공합니다.

> 🔥 **OpenTelemetry First**: 표준 Span, Trace, Metrics를 활용한 현대적 관측성  
> ⚡ **AWS Native**: X-Ray, CloudWatch Logs/Metrics 직접 연동  
> 🔄 **Migration Ready**: 기존 MoniKit 설정과 100% 호환

---

## 🎯 핵심 가치

### ✨ 표준화된 관측성
- **W3C Trace Context**: 분산 트레이싱 표준 준수
- **OpenTelemetry Semantic Conventions**: 업계 표준 메타데이터
- **OTLP (OpenTelemetry Protocol)**: 표준 전송 프로토콜

### 🏗️ AWS 클라우드 네이티브
- **X-Ray Integration**: 분산 트레이싱 및 서비스 맵
- **CloudWatch Logs**: 구조화된 로그 자동 전송
- **CloudWatch Metrics**: 커스텀 메트릭 수집
- **ADOT Collector**: AWS Distro for OpenTelemetry 완전 지원

### 🔄 마이그레이션 친화적
- **Zero Breaking Changes**: 기존 설정 및 코드 그대로 유지
- **Gradual Migration**: 병렬 실행을 통한 단계적 전환
- **Feature Parity**: 기존 MoniKit 기능 100% 지원

---

## 🚀 빠른 시작

### 1. 의존성 추가

```gradle
dependencies {
    // 기존 의존성 유지
    implementation 'com.ryuqq:monikit-starter-web:1.1.3'
    
    // 🔥 OpenTelemetry 모듈 추가
    implementation 'com.ryuqq:monikit-otel:2.0.0'
}
```

### 2. 한 줄 설정으로 활성화

```yaml
monikit:
  otel:
    enabled: true  # 🔥 OpenTelemetry 활성화
  
  # 기존 설정은 그대로 유지 (선택사항)
  logging:
    log-enabled: true
    dynamic-matching:
      - classNamePattern: ".*Service"
        methodNamePattern: ".*"
        when: "#executionTime > 100"
        thresholdMillis: 100
        tag: "slow-service"
```

### 3. 즉시 확인

```bash
# OpenTelemetry 활성화 로그 확인
tail -f application.log | grep "OpenTelemetry ExecutionLoggingAspect activated"

# OTLP 메트릭 확인 (기본: http://localhost:4318)
curl -X POST http://localhost:4318/v1/traces -d '{}'
```

---

## 🏗️ 아키텍처 설계

### OpenTelemetry 우선 접근

```text
[MoniKit 기존 방식]
ExecutionLoggingAspect → LogEntry → LogSink → ELK/Prometheus

[OpenTelemetry 새로운 방식]  
OtelExecutionLoggingAspect → Span → OTLP → AWS X-Ray/CloudWatch
```

### 핵심 구성요소

| 컴포넌트 | 역할 |
|----------|------|
| `OtelExecutionLoggingAspect` | 기존 `ExecutionLoggingAspect` 대체 |
| `OtelTraceIdProvider` | W3C Trace Context 기반 TraceId 제공 |
| `OtelAutoConfiguration` | Spring Boot 자동 구성 |
| `OtelExporterConfig` | OTLP Exporter 설정 관리 |

### 자동 전환 메커니즘

```yaml
# OpenTelemetry 비활성화 (기본값)
monikit.otel.enabled: false
→ 결과: ExecutionLoggingAspect 사용 (기존 방식)

# OpenTelemetry 활성화
monikit.otel.enabled: true  
→ 결과: OtelExecutionLoggingAspect 사용 (새로운 방식)
```

---

## 📊 OpenTelemetry 데이터 구조

### Span Attributes (기존 LogEntry 정보 포함)

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

### 기존 vs OpenTelemetry 비교

| 기능 | 기존 MoniKit | OpenTelemetry |
|------|---------------|---------------|
| **실행 시간 측정** | 커스텀 로그 | Span duration (자동) |
| **메트릭 수집** | 커스텀 Hook | OpenTelemetry Metrics |
| **분산 추적** | ThreadLocal TraceId | 표준 TraceId/SpanId |
| **로그 상관관계** | MDC 수동 주입 | 자동 주입 |
| **AWS 연동** | ELK + Prometheus | CloudWatch + X-Ray (네이티브) |
| **표준 준수** | 커스텀 구현 | W3C, OTEL 표준 |

---

## 🔧 고급 설정

### 1. OTLP Exporter 설정

```yaml
monikit:
  otel:
    enabled: true
    otlp:
      # Traces 전송 엔드포인트
      traces-endpoint: "http://localhost:4318/v1/traces"
      # Metrics 전송 엔드포인트  
      metrics-endpoint: "http://localhost:4318/v1/metrics"
      # Logs 전송 엔드포인트
      logs-endpoint: "http://localhost:4318/v1/logs"
      # 압축 설정
      compression: "gzip"
      # 타임아웃 설정
      timeout: "10s"
```

### 2. AWS ADOT Collector 연동

**docker-compose.yml**
```yaml
version: '3.8'
services:
  adot-collector:
    image: public.ecr.aws/aws-observability/aws-otel-collector:latest
    command: ["--config=/etc/otel-agent-config.yaml"]
    environment:
      - AWS_REGION=ap-northeast-2
    volumes:
      - ./adot-config.yaml:/etc/otel-agent-config.yaml
    ports:
      - "4317:4317"   # OTLP gRPC receiver
      - "4318:4318"   # OTLP HTTP receiver
```

**adot-config.yaml**
```yaml
receivers:
  otlp:
    protocols:
      grpc:
        endpoint: 0.0.0.0:4317
      http:
        endpoint: 0.0.0.0:4318

exporters:
  awsxray:
    region: ap-northeast-2
  awscloudwatchlogs:
    region: ap-northeast-2
    log_group_name: "/aws/application/monikit"
  awscloudwatchmetrics:
    region: ap-northeast-2
    namespace: "MoniKit/Application"

service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [awsxray]
    logs:
      receivers: [otlp]
      exporters: [awscloudwatchlogs]  
    metrics:
      receivers: [otlp]
      exporters: [awscloudwatchmetrics]
```

### 3. 샘플링 설정

```yaml
monikit:
  otel:
    enabled: true
    sampling:
      # 개발환경: 100% 샘플링
      ratio: 1.0
      # 운영환경: 1% 샘플링  
      # ratio: 0.01
```

---

## 🔄 마이그레이션 가이드

### Phase 1: 병렬 실행 (권장)

```yaml
monikit:
  logging:
    log-enabled: true   # 기존 시스템 유지
  otel:
    enabled: true       # 새 시스템 활성화
```

**결과**: 두 시스템이 동시 실행되어 안전한 비교 검증 가능

### Phase 2: 검증 단계

1. **AWS X-Ray 콘솔** 에서 트레이스 확인
2. **CloudWatch Logs** 에서 구조화된 로그 확인  
3. **기존 대시보드** 와 **새 Grafana** 메트릭 비교

### Phase 3: 완전 전환

```yaml
monikit:
  logging:
    log-enabled: false  # 기존 시스템 비활성화
  otel:
    enabled: true       # OpenTelemetry만 사용
```

---

## 📈 AWS 통합 대시보드

### 1. X-Ray 서비스 맵
```text
[웹 애플리케이션] → [UserService] → [Database]
                  ↓
              [External API]
```

### 2. CloudWatch Insights 쿼리
```sql
fields @timestamp, monikit.execution.time_ms, code.function
| filter monikit.slow_execution = true
| sort @timestamp desc
| limit 100
```

### 3. Managed Grafana 대시보드
```json
{
  "title": "MoniKit OpenTelemetry Dashboard",
  "panels": [
    {
      "title": "Trace Duration Distribution", 
      "type": "histogram",
      "targets": [
        {
          "expr": "histogram_quantile(0.95, otel_span_duration_bucket)",
          "legendFormat": "95th percentile"
        }
      ]
    }
  ]
}
```

---

## 🔍 트러블슈팅

### 1. ADOT Collector 연결 실패

```bash
# 컨테이너 상태 확인
docker-compose ps adot-collector

# 로그 확인
docker-compose logs adot-collector

# 연결 테스트
curl -X POST http://localhost:4318/v1/traces \
  -H "Content-Type: application/json" \
  -d '{}'
```

### 2. AWS 권한 오류

```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Action": [
        "xray:PutTraceSegments",
        "xray:PutTelemetryRecords",
        "logs:CreateLogGroup",
        "logs:CreateLogStream", 
        "logs:PutLogEvents",
        "cloudwatch:PutMetricData"
      ],
      "Resource": "*"
    }
  ]
}
```

### 3. 성능 영향 최적화

```yaml
monikit:
  otel:
    enabled: true
    # 배치 처리로 성능 최적화
    batch:
      max-export-batch-size: 512
      export-timeout: "30s"
      schedule-delay: "5s"
    # 메모리 사용량 제한
    resource:
      max-attributes: 128
```

---

## 🚀 마이그레이션 이점

### 운영 효율성
- **표준화**: 업계 표준 도구와 완벽 호환
- **자동화**: AWS 서비스 네이티브 통합으로 설정 간소화  
- **확장성**: 클라우드 스케일 자동 처리

### 개발 생산성  
- **호환성**: 기존 코드 변경 없이 업그레이드
- **가시성**: X-Ray 서비스 맵을 통한 직관적 시각화
- **디버깅**: 분산 트레이싱으로 문제점 빠른 식별

### 비용 최적화
- **샘플링**: 지능적 샘플링으로 비용 절감
- **압축**: GZIP 압축으로 네트워크 비용 절감
- **배치 처리**: 효율적인 데이터 전송

---

## 🔗 연관 문서

- [MIGRATION_GUIDE.md](../MIGRATION_GUIDE.md): 상세한 마이그레이션 가이드
- [QUICK_START.md](../QUICK_START.md): 30초 빠른 시작 가이드  
- [AWS OpenTelemetry 문서](https://aws-otel.github.io/docs/introduction)
- [OpenTelemetry Java 문서](https://opentelemetry.io/docs/instrumentation/java/)

---

(c) 2024 Ryu-qqq. MoniKit OpenTelemetry 프로젝트