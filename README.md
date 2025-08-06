# 🚀 MoniKit: Enterprise-Grade Observability for Modern Java Applications

[![Java](https://img.shields.io/badge/Java-21+-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3+-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![OpenTelemetry](https://img.shields.io/badge/OpenTelemetry-1.31+-blue.svg)](https://opentelemetry.io/)
[![AWS](https://img.shields.io/badge/AWS-X--Ray%20%7C%20CloudWatch-orange.svg)](https://aws.amazon.com/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**MoniKit**은 **장애를 빠르게 감지하고 신속히 대응**할 수 있도록 설계된 **현대적 관측성 프레임워크**입니다.  
소규모 팀부터 엔터프라이즈까지, **복잡한 인프라 없이도** 프로덕션급 모니터링을 구현할 수 있습니다.

## ✨ 핵심 가치

### 🎯 **자동화된 장애 감지**
수동 로그 검색에서 벗어나 **구조화된 로깅**과 **지능적 알림**으로 장애를 실시간 감지

### ⚡ **제로 구성 통합**
의존성 하나로 **AOP 로깅**, **메트릭 수집**, **분산 추적**을 자동 적용

### 🌐 **업계 표준 준수**
**OpenTelemetry** 기반으로 AWS, Prometheus, Grafana 등 모든 도구와 네이티브 연동

### 📈 **프로덕션 검증**
메모리 보호, 성능 최적화, 장애 복구 등 **엔터프라이즈급 안정성** 확보


## 🚀 빠른 시작

### 1. 의존성 추가

```gradle
dependencies {
    // 🔥 OpenTelemetry 기반 (권장)
    implementation 'com.ryuqq:monikit-otel:2.0.0'
    
    // 또는 기존 버전
    implementation 'com.ryuqq:monikit-starter-web:1.1.3'
}
```

### 2. 30초 설정

```yaml
monikit:
  otel:
    enabled: true  # 🔥 OpenTelemetry 활성화
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
# 애플리케이션 시작 후 로그 확인
tail -f logs/application.log | grep "ExecutionLoggingAspect activated"

# Prometheus 메트릭 확인  
curl http://localhost:8080/actuator/prometheus | grep monikit
```

---

## 📦 모듈 아키텍처

MoniKit은 **모듈형 설계**로 필요한 기능만 선택적으로 사용할 수 있습니다.

### 🎯 Core Modules (v1.1.0)

| 모듈 | 역할 | 상태 |
|------|------|------|
| [`monitoring-core`](monitoring-core/README.md) | 🏗️ 순수 Java 기반 로깅 코어 | ✅ Active |
| [`monitoring-config`](monitoring-config/README.md) | ⚙️ 설정 관리 및 SpEL 규칙 | ✅ Active |
| [`monitoring-starter`](monitoring-starter/README.md) | 🚀 Spring Boot 통합 스타터 | ✅ Active |
| [`monitoring-starter-web`](monitoring-starter-web/README.md) | 🌐 웹 애플리케이션 전용 | ✅ Active |
| [`monitoring-metric`](monitoring-metric/README.md) | 📊 Micrometer/Prometheus 통합 | ✅ Active |
| [`monitoring-otel`](monitoring-otel/README.md) | 🔥 OpenTelemetry 표준 지원 | 🆕 New |

### 🗑️ Deprecated Modules

| 모듈 | 대체 방안 | 제거 예정 |
|------|-----------|-----------|
| `monitoring-jdbc` | OpenTelemetry JDBC Instrumentation | v2.0.0 |
| `monitoring-slf4j` | OpenTelemetry Logs Bridge | v2.0.0 |
| `monitoring-starter-batch` | 사용 빈도 낮음 | v2.0.0 |

---

## 🎯 주요 기능

### ⚡ AOP 기반 자동 로깅
```java
@Service
public class UserService {
    
    public User createUser(String name) {
        // 🔥 자동으로 실행 시간, 인자, 결과 로깅
        return userRepository.save(new User(name));
    }
}
```

### 📊 실시간 메트릭 수집
```prometheus
# HTTP 응답 시간
http_response_duration_seconds{path="/api/users",status="200",quantile="0.95"} 0.150

# SQL 쿼리 성능
sql_query_duration_seconds{query_type="SELECT",table="users",quantile="0.99"} 0.045

# 비즈니스 메트릭
execution_detail_count{class="UserService",method="createUser",tag="user-registration"} 156
```

### 🔍 지능적 동적 필터링
```yaml
monikit:
  logging:
    dynamic-matching:
      # 외부 API 호출만 로깅
      - classNamePattern: ".*ExternalClient"
        when: "#executionTime > 1000"
        tag: "external-api"
      
      # 느린 데이터베이스 작업
      - classNamePattern: ".*Repository" 
        when: "#executionTime > 500"
        tag: "slow-query"
```

### 🛡️ 프로덕션 안정성
- **메모리 보호**: MAX_TIMER_COUNT=100 으로 메트릭 폭발 방지
- **자동 정규화**: `/api/users/123` → `/api/users/{id}` 경로 정규화  
- **성능 최적화**: ConcurrentHashMap 기반 캐싱
- **장애 복구**: Graceful degradation 및 자동 복구

---

## 🏗️ OpenTelemetry 마이그레이션

MoniKit v2.0은 **업계 표준 OpenTelemetry**로 완전 전환됩니다.

### 🔥 마이그레이션 이점

| 기존 MoniKit | OpenTelemetry |
|--------------|---------------|
| 커스텀 TraceId | W3C Trace Context 표준 |
| 수동 로그 상관관계 | 자동 Span 연결 |
| ELK + Prometheus | AWS X-Ray + CloudWatch 네이티브 |
| 커스텀 메트릭 | OpenTelemetry Metrics 표준 |

### 📈 AWS 클라우드 네이티브
```yaml
monikit:
  otel:
    enabled: true
    # AWS ADOT Collector 연동
    otlp:
      traces-endpoint: "http://adot-collector:4318/v1/traces"
      metrics-endpoint: "http://adot-collector:4318/v1/metrics"
```

**결과**: X-Ray 서비스 맵, CloudWatch 대시보드, Managed Grafana 자동 연동

---

## 📊 모니터링 대시보드

### Grafana 대시보드 예시
```json
{
  "title": "MoniKit Application Performance",
  "panels": [
    {
      "title": "Response Time P95",
      "expr": "histogram_quantile(0.95, rate(http_response_duration_seconds_bucket[5m]))"
    },
    {
      "title": "Error Rate", 
      "expr": "rate(http_response_count{status=~\"5..\"}[5m]) / rate(http_response_count[5m])"
    }
  ]
}
```

### AWS X-Ray 서비스 맵
```text
[Web App] → [User Service] → [Database]
     ↓
[External API] → [Payment Service]
```

---

## 🔧 고급 설정

### 성능 튜닝
```yaml
monikit:
  logging:
    # 패키지 필터링
    allowed-packages:
      - "com.company.core"
      - "com.company.service"
    
    # 샘플링 (10% 로깅)
    sampling-rate: 0.1
  
  metrics:
    # 메트릭 수집 최적화
    max-timer-count: 50
    batch-size: 1000
```

### 커스텀 Hook 확장
```java
@Component
public class SlackNotificationHook implements LogAddHook {
    
    @Override
    public void onAdd(LogEntry logEntry) {
        if (logEntry.getLogLevel() == LogLevel.ERROR) {
            slackClient.sendAlert(logEntry.toString());
        }
    }
}
```

---

## 📚 완전한 가이드

- **[마이그레이션 가이드](MIGRATION_GUIDE.md)**: OpenTelemetry 전환 방법
- **[빠른 시작](QUICK_START.md)**: 30초 설정 가이드
- **[패키지별 상세 문서](#-모듈-아키텍처)**: 각 모듈 심화 가이드

---

## 🤝 커뮤니티 & 지원

- **🐛 버그 신고**: [GitHub Issues](https://github.com/ryu-qqq/moni-kit/issues)
- **💡 기능 요청**: [GitHub Discussions](https://github.com/ryu-qqq/moni-kit/discussions)  
- **📖 문서**: 각 패키지 README.md 참조
- **⭐ 별점**: 프로젝트가 도움되셨다면 별점을 눌러주세요!

---

*"장애를 빠르게 감지하고 신속히 대응할 수 있도록 설계되었는가?" - 이 질문에 대한 MoniKit의 답입니다.*

© 2024 Ryu-qqq. MoniKit Project.
