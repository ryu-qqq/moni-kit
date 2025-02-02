# **MoniKit - 효과적인 모니터링을 위한 로깅 & 메트릭 수집 라이브러리**

**1. 프로젝트 소개 **
   - MoniKit은 Spring Boot 애플리케이션에서 로그를 체계적으로 수집하고, 성능 메트릭을 측정할 수 있도록 설계된 라이브러리입니다.
   - 이 라이브러리는 ELK (Elasticsearch, Logstash, Kibana)와 Prometheus/Grafana를 활용하여 API 응답 시간, SQL 실행 빈도, 에러 발생 비율 등을 모니터링할 수 있도록 돕습니다.

--- 

**✅ MoniKit을 만든 이유**

- 효율적인 로깅: 로그를 역할(Role) 기반으로 정리하여, logType과 logLevel을 활용한 체계적인 분석이 가능하도록 함.
- ELK와 연동 가능: logType, logLevel, traceId를 포함한 JSON 로그를 남겨 Kibana에서 쉽게 검색 및 필터링 가능.
- Prometheus/Grafana와 연동 가능: API 실행 시간, SQL 쿼리 실행 빈도 등 메트릭을 수집하여 시각화할 수 있도록 지원.
- 자동화된 실행 흐름 추적: AOP 기반으로 모든 요청의 실행 흐름을 자동으로 로깅하여 디버깅이 용이함.


2. 주요 기능
   📌 로그를 역할별로 구분하여 ELK에서 효율적으로 분석
   MoniKit은 단순한 텍스트 기반 로그가 아니라, JSON 포맷의 구조화된 로그를 남겨 분석을 쉽게 합니다.

ExecutionTimeLog → API 및 메서드 실행 시간 기록 (성능 분석)
BusinessEventLog → 주요 비즈니스 이벤트 로깅 (주문 생성, 결제 완료 등)
ExceptionLog → 예외 발생 시 스택 트레이스를 포함한 상세 로그 저장
📌 예제: API 실행 시간 로그 (ELK 저장)

```
{
"timestamp": "2024-02-01T12:00:00Z",
"traceId": "abc123",
"logType": "execution_time",
"logLevel": "DEBUG",
"service": "order-service",
"method": "placeOrder",
"executionTime": 230
}
```

✅ Kibana에서 logType:execution_time을 필터링하면 API 응답 시간 분석 가능.

📌 Prometheus에서 API 실행 시간 및 SQL 실행 빈도 모니터링
MoniKit은 API 응답 시간과 SQL 실행 빈도를 Prometheus 메트릭으로 저장하여 Grafana에서 실시간 모니터링이 가능합니다.

📌 예제: API 응답 시간 수집
```
java
public void recordExecutionTime(String endpoint, long executionTime) {
Timer.builder("monikit_api_execution_time")
.tag("server", "monikit-server")
.tag("endpoint", endpoint)
.register(meterRegistry)
.record(executionTime, TimeUnit.MILLISECONDS);
}
```
✅ Grafana에서 monikit_api_execution_time을 조회하면 어느 API가 가장 느린지 시각적으로 분석 가능.

3. 사용 방법
   📌 1) Gradle 의존성 추가
```   
gradle
   dependencies {
   implementation 'com.ryuqq:monikit-core:0.0.1'
   implementation 'com.ryuqq:monikit-starter:0.0.1' // Spring Boot에서 사용할 경우
   }
 ```
   📌 2) 기본적인 로깅 사용 예제
   1️⃣ API 실행 시간 로깅
```
   java
   public class OrderService {
   private final MoniKitLogger logger = new MoniKitLogger(OrderService.class);

   public void placeOrder() {
   long start = System.currentTimeMillis();
   // 주문 생성 로직...
   long executionTime = System.currentTimeMillis() - start;

        logger.info(new ExecutionTimeLog(TraceIdGenerator.getTraceId(), "OrderService", "placeOrder", executionTime).toJson());
   }
   }
```


   ✅ ExecutionTimeLog는 API 실행 시간을 자동으로 수집하여 ELK에 저장.

2️⃣ SQL 실행 빈도 로깅

```
java
public void executeQuery(String query) {
long start = System.currentTimeMillis();
// 쿼리 실행...
long executionTime = System.currentTimeMillis() - start;

    logger.info(new BusinessEventLog(TraceIdGenerator.getTraceId(), "query_execution", query).toJson());
}
```


✅ Kibana에서 logType:business_event을 필터링하면 자주 실행되는 SQL 조회 가능.

3️⃣ 예외 발생 로그 저장
```
java
@RestControllerAdvice
public class ExceptionLoggingAdvice {
private final MoniKitLogger logger = new MoniKitLogger(ExceptionLoggingAdvice.class);

    @ExceptionHandler(Exception.class)
    public void handleException(Exception e) {
        logger.error(new ExceptionLog(TraceIdGenerator.getTraceId(), e).toJson());
    }
}
```
✅ Kibana에서 logType:exception을 필터링하면 에러 발생 내역 분석 가능.

4. ELK & Prometheus 연동
   📌 1) Logback을 이용한 ELK 연동
   📌 logback-spring.xml 설정 예제
```
xml
<configuration>
<appender name="LOGSTASH" class="net.logstash.logback.appender.LogstashTcpSocketAppender">
<destination>logstash:5044</destination>
<encoder class="net.logstash.logback.encoder.LogstashEncoder" />
</appender>

    <root level="INFO">
        <appender-ref ref="LOGSTASH"/>
    </root>
</configuration>

```
✅ Logback이 JSON 포맷으로 로그를 남기고, Logstash가 이를 ELK로 전송.

📌 2) Prometheus & Grafana 연동
📌 prometheus.yml 설정

```
yaml
scrape_configs:
- job_name: 'monikit'
  metrics_path: '/actuator/prometheus'
  static_configs:
    - targets: ['monikit-server:8080']
      ✅ Prometheus에서 API 실행 시간, SQL 실행 빈도 등 메트릭을 수집하고,
      ✅ Grafana에서 API별 응답 시간 대시보드를 구성하여 실시간 모니터링 가능.
```

5. Kibana & Grafana 대시보드
   🚀 이제 Kibana/Grafana에서 MoniKit 데이터를 활용하여 대시보드를 만들 수 있습니다.

✅ 📌 Kibana 대시보드
API 응답 시간 트렌드 (logType:execution_time 기반)
가장 자주 실행된 SQL 쿼리 (logType:business_event 기반)
최근 7일 동안 발생한 에러 로그 (logType:exception AND logLevel:ERROR 기반)
traceId 기반 트랜잭션 분석 (하나의 요청에서 발생한 모든 로그 검색)
✅ 📌 Grafana 대시보드
최근 5분 동안 가장 느린 API (monikit_api_execution_time)
SQL 실행 시간이 가장 긴 쿼리 TOP 10 (monikit_query_execution_time)
전체 시스템 응답 시간 평균값 (monikit_api_execution_time 평균값)
최근 5분 동안 발생한 에러 비율 (monikit_exception_count)


6. 결론
   ✅ MoniKit은 ELK와 Prometheus를 연동하여 효과적인 모니터링을 제공합니다.
   ✅ 자동으로 API 응답 시간, SQL 실행 빈도, 에러 로그를 수집하고, Kibana/Grafana에서 분석할 수 있습니다.
   ✅ 로그 및 메트릭 데이터를 활용하여 서비스 성능을 최적화하고, 장애를 빠르게 탐지할 수 있습니다.

🔥 이제 MoniKit을 활용하여 여러분의 Spring Boot 애플리케이션을 더 쉽게 모니터링하세요! 🚀