# Monitoring-Core

## MoniKit Core 패키지 주요 로깅 클래스 설명

### **로그**
- **`LogEntry`** 인터페이스
   MoniKit의 로그는 `LogEntry` 인터페이스를 구현하여 생성됩니다. 이 인터페이스는 ELK 및 Prometheus와 연동할 수 있는 구조화된 로그 데이터를 생성합니다.
- **`LogType`**
   로그 유형을 정의하는 Enum입니다. 이 값들은 ELK와 Kibana에서 로그를 필터링하는 데 사용됩니다.

### **로그 컨텍스트 관리**
- **`LogEntryContext`**: `ThreadLocal` 기반으로 개별 요청의 로그를 저장.
- **`LogEntryContextManager` (interface)**: 로그 컨텍스트의 추가, 조회, 삭제를 관리.
- **`DefaultLogEntryContextManager`**: `LogEntryContextManager`의 기본 구현체.
- **`ThreadContextHandler` (interface)**: 멀티스레드 환경에서 로그 컨텍스트를 유지하도록 지원.
- **`DefaultThreadContextHandler`**: `ThreadContextHandler`의 기본 구현체.
- **`ThreadContextPropagator`**: 부모 스레드의 로그 컨텍스트를 자식 스레드로 전파.
- **`TraceIdProvider` (interface)**: 현재 요청의 `traceId`를 제공.


### **노티파이어**
- **`LogNotifier` (interface)**: 로그를 외부 시스템 (ELK, Slack 등)에 전송하는 역할.
- **`DefaultLogNotifier`**: 기본 `System.out.println()` 기반 로거.
- **`ErrorLogNotifier` (interface)**: 에러 로그 감지 시 실행될 후크 제공.
- **`DefaultErrorLogNotifier`**: 기본 에러 로그 감지기.

### **SQL 로깅 및 성능 측정**
- **`QueryLoggingService` (interface)**: SQL 실행 로그 및 성능 모니터링 기능 제공.
- **`SqlParameterHolder`**: SQL 실행 시 바인딩된 값을 관리 (`ThreadLocal` 기반).
- **`DataSourceProvider` (interface)**: 현재 사용 중인 `DataSource`의 이름을 제공.
- **`MetricCollector` (interface)**: SQL 실행 시간을 기록하는 메트릭 수집기.
- **`QueryPerformanceEvaluator`**: SQL 실행 시간이 지정된 임계값을 초과했는지 평가.
- **`LoggingPreparedStatement`**: SQL 실행을 감시하고 로깅하는 `PreparedStatement` 프록시.
- **`LoggingResultSet`**: `ResultSet`을 감싸서 `close()` 시 SQL 실행 정보를 기록.