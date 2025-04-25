# MoniKit Starter JDBC (v1.1.2)

## 📌 개요

`monikit-starter-jdbc`는 JDBC 기반의 SQL 실행을 **자동으로 감시하고**, **실행 로그 및 메트릭을 수집**할 수 있는 경량 로깅 모듈입니다.  
이 스타터는 기존 `DataSource`를 감싸는 프록시 형태로 동작하며, 애플리케이션의 **쿼리 실행 시간**, **슬로우 쿼리 감지**, **Trace ID 기반의 추적**을 지원합니다.
> 내부적으로 `ObjectProvider<DataSource>`를 통해 순환 참조를 방지하며,  
> 이미 등록된 `@Primary` DataSource 가 감싸져도 무한 래핑은 발생하지 않습니다.
---

## ✅ 지원 대상

- Spring Boot 기본 JDBC (`JdbcTemplate`, `DataSource`)
- HikariCP, Tomcat, DBCP 등 **커넥션 풀 사용 가능**
- `javax.sql.DataSource`를 기반으로 작동

## ⚙️ 기본 기능

- `PreparedStatement`/`Connection`을 프록시 객체로 감싸 SQL 실행 정보 로깅
- 실행 시간 기반의 로그 레벨 분류 (`INFO`, `WARN`, `ERROR`)
- ThreadLocal 기반 파라미터 추적 기능
- `traceId` 기반의 추적 ID 관리 (MDC)
- 최소한의 설정으로 자동 구성

---

## 🧩 주요 클래스 흐름

```text
[사용자 지정 originalDataSource]
        |
        ▼
[LoggingDataSource]
        |
        ▼
[LoggingConnection]
        |
        ▼
[LoggingPreparedStatementFactory]
        |
        ▼
[LoggingPreparedStatement]
        |
        ▼
[QueryLoggingService]
        |
        ▼
[LogEntryContextManager → 로그 수집]
```

---

## 🧱 주요 구성 요소

### 1. `LoggingDataSource`
기존 `DataSource`를 감싸서 JDBC Connection을 프록시로 반환합니다.
```java
@Override
public Connection getConnection() {
    return new LoggingConnection(super.getConnection(), preparedStatementFactory);
}
```

### 2. `LoggingConnection`
`prepareStatement(...)` 메서드를 감지해 `LoggingPreparedStatement`로 감쌉니다.

### 3. `LoggingPreparedStatementFactory`
`PreparedStatement`를 감싸는 프록시 생성 팩토리입니다.
- `QueryLoggingService`를 통해 실행 시점 로깅을 담당합니다.
- `traceId`는 MDC를 통해 추출합니다.

### 4. `LoggingPreparedStatement`
- 실행 시간 측정
- 쿼리와 파라미터 기록
- `try-with-resources` 패턴으로 `SqlParameterHolder`를 자동 관리

### 5. `SqlParameterHolder`
- `ThreadLocal` 기반으로 파라미터를 수집
- 파라미터 목록을 `toString()`으로 문자열 변환 제공

### 6. `QueryLoggingService` (인터페이스)
- 실제 로깅 및 메트릭 수집을 담당하는 인터페이스

### 7. `DefaultQueryLoggingService`
- 기본 구현체
- 쿼리 실행 시간에 따라 로그 레벨 분류
- `LogEntryContextManager`에 로그 추가

### 8. `DataSourceProvider`
- 현재 데이터소스의 이름을 감지해 반환
- 사용자 정의 가능

### 9. `DefaultDataSourceProvider`
- `JDBC URL`에서 DB명을 추출하거나, 인메모리 DB 여부를 판단합니다.

---

## 🔧 자동 설정

`DataSourceLoggingConfig`와 `QueryLoggingConfig`는 Spring Boot의 AutoConfiguration으로 등록됩니다.

- `monikit.logging.log-enabled=true`
- `monikit.logging.datasource-logging-enabled=true`  
  → 이 두 조건이 만족될 때만 `LoggingDataSource`가 적용됩니다.

---

## 📄 관련 설정 (application.yml)

```yaml
monikit.logging:
  log-enabled: true
  datasource-logging-enabled: true
  slow-query-threshold-ms: 1000
  critical-query-threshold-ms: 5000
  allowed-packages:
    - "com.ryuqq"
    - "com.monikit"
  dynamic-matching:
    - classNamePattern: "^External.*Client"
      methodNamePattern: ".*"
      when: "#executionTime > 1000"
      thresholdMillis: 1000
      tag: "external-api"

```

---

## 📌 참고할 클래스

| 클래스명 | 설명 |
|----------|------|
| `LoggingDataSource` | 원본 DataSource를 감싸는 프록시 |
| `LoggingConnection` | JDBC Connection 프록시 |
| `LoggingPreparedStatement` | PreparedStatement 프록시 |
| `QueryLoggingService` | 로깅/메트릭을 수행하는 인터페이스 |
| `DefaultQueryLoggingService` | 기본 로깅 구현체 |
| `SqlParameterHolder` | 파라미터 추적 관리 클래스 |
| `DataSourceProvider` | DB 이름을 추출하는 전략 |
| `DefaultDataSourceProvider` | 기본 구현체 (JDBC URL 파싱) |

---

## 🧪 테스트 팁
- 비활성화 테스트는 `log-enabled=false` 설정으로 확인 가능



