# MoniKit OpenTelemetry 마이그레이션 가이드

## 🎯 개요

MoniKit v2.0.0부터 **OpenTelemetry 표준**을 도입하여 AWS 관측성 서비스와 완전히 통합됩니다.

### 주요 변경사항
- ✅ **표준화**: W3C Trace Context, OpenTelemetry 표준 준수
- ✅ **AWS 통합**: X-Ray, CloudWatch Logs/Metrics, Managed Grafana
- ✅ **호환성 유지**: 기존 설정 및 SpEL 규칙 그대로 사용
- ⚠️ **Deprecation**: 기존 커스텀 구현체들은 2.0.0에서 deprecated

---

## 🚀 빠른 시작

### 1. 의존성 변경

**기존 (v1.x)**
```gradle
dependencies {
    implementation 'com.monikit:monikit-starter-web:1.1.3'
}
```

**신규 (v2.0+)**
```gradle
dependencies {
    implementation 'com.monikit:monikit-starter-otel:2.0.0'
}
```

### 2. 설정 추가

**application.yml**
```yaml
monikit:
  otel:
    enabled: true  # 🔥 이 설정만 추가하면 자동 전환!
    traces:
      endpoint: "http://localhost:4317"  # ADOT Collector
    logs:
      endpoint: "http://localhost:4317"
    metrics:
      endpoint: "http://localhost:4317"
  
  # 기존 설정은 그대로 유지
  logging:
    log-enabled: true
    dynamic-matching:
      - classNamePattern: ".*Service"
        methodNamePattern: ".*"
        when: "#executionTime > 100"
        thresholdMillis: 100
        tag: "slow-service"
```

---

## 🔄 단계별 마이그레이션

### Phase 1: 병렬 실행 (권장)
기존 시스템과 OpenTelemetry를 동시에 실행하여 안전하게 전환

```yaml
monikit:
  logging:
    log-enabled: true  # 기존 시스템 유지
  otel:
    enabled: true      # 새 시스템 활성화
```

### Phase 2: 검증 및 비교
- AWS X-Ray 콘솔에서 트레이스 확인
- CloudWatch Logs에서 로그 확인
- Grafana 대시보드에서 메트릭 비교

### Phase 3: 완전 전환
```yaml
monikit:
  logging:
    log-enabled: false  # 기존 시스템 비활성화
  otel:
    enabled: true       # OpenTelemetry만 사용
```

---

## 📊 AWS 인프라 설정

### 1. ADOT Collector 배포

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
    log_stream_name: "application-logs"
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

### 2. IAM 권한 설정

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

---

## 🔧 코드 변경사항

### Deprecated 클래스 교체

**기존 코드**
```java
@Autowired
private DefaultTraceIdProvider traceIdProvider;  // ❌ Deprecated

@Autowired
private ExecutionLoggingAspect aspect;  // ❌ Deprecated
```

**신규 코드**
```java
@Autowired
private OtelTraceIdProvider traceIdProvider;  // ✅ OpenTelemetry 기반

@Autowired
private OtelExecutionLoggingAspect aspect;  // ✅ OpenTelemetry 기반
```

### 커스텀 Hook 유지
기존 Hook 시스템은 그대로 사용 가능합니다.

```java
@Component
public class CustomLogHook implements LogAddHook {
    @Override
    public void onAdd(LogEntry logEntry) {
        // 기존 로직 그대로 유지
        if (logEntry.getLogType() == LogType.EXCEPTION) {
            // Slack 알림 등
        }
    }
}
```

---

## 📈 모니터링 대시보드

### AWS Managed Grafana 대시보드

**서비스 맵**
- X-Ray 서비스 맵 자동 연동
- 마이크로서비스 간 의존성 시각화

**성능 메트릭**
```json
{
  "title": "Application Performance",
  "panels": [
    {
      "title": "Response Time",
      "type": "graph",
      "targets": [
        {
          "expr": "monikit_execution_time_ms",
          "legendFormat": "{{method_name}}"
        }
      ]
    },
    {
      "title": "Error Rate",
      "type": "stat",
      "targets": [
        {
          "expr": "rate(monikit_exceptions_total[5m])"
        }
      ]
    }
  ]
}
```

---

## ⚠️ 주의사항

### 성능 영향
- OpenTelemetry 초기 오버헤드: **5-10%**
- 메모리 사용량 증가: **10-15%**
- 네트워크 트래픽 증가: OTLP 전송으로 인한 추가 트래픽

### 비용 고려사항
- **AWS X-Ray**: 트레이스 수집 및 저장 비용
- **CloudWatch Logs**: 로그 수집 및 저장 비용
- **CloudWatch Metrics**: 커스텀 메트릭 비용

### 호환성
- **Spring Boot**: 2.7+ 권장
- **Java**: 11+ 필수
- **AWS SDK**: 2.20+ 권장

---

## 🆘 문제 해결

### 자주 발생하는 문제

**1. ADOT Collector 연결 실패**
```yaml
# 해결: 엔드포인트 확인
monikit:
  otel:
    traces:
      endpoint: "http://adot-collector:4317"  # 컨테이너 이름 사용
```

**2. AWS 권한 오류**
```bash
# 해결: IAM 역할 확인
aws sts get-caller-identity
aws iam get-role --role-name YourECSTaskRole
```

**3. 트레이스가 X-Ray에 나타나지 않음**
```yaml
# 해결: 샘플링 설정 확인
monikit:
  otel:
    traces:
      sampling-ratio: 1.0  # 개발 환경에서는 100% 샘플링
```

---

## 📞 지원

- **GitHub Issues**: [monikit/issues](https://github.com/ryu-qqq/moni-kit/issues)
- **문서**: [MoniKit OpenTelemetry Guide](./monitoring-otel/README.md)
- **예제**: [examples/](./examples/)

---

**🎉 축하합니다! MoniKit v2.0으로 성공적으로 마이그레이션되었습니다.**
