# Monitoring-Core

## MoniKit Core 패키지 주요 로깅 클래스 설명

1. `LogEntry` 인터페이스
   MoniKit의 로그는 `LogEntry` 인터페이스를 구현하여 생성됩니다. 이 인터페이스는 ELK 및 Prometheus와 연동할 수 있는 구조화된 로그 데이터를 생성합니다.

주요 메서드:
* `getTimestamp()`: 로그 생성 시각 (UTC 기준)
* `getTraceId()`: 요청 또는 트랜잭션의 고유 ID
* `getLogType()`: 로그의 유형 (LogType Enum)
* `getLogLevel()`: 로그의 심각도 (예: TRACE, DEBUG, INFO, WARN, ERROR)
* `toString()`: 로그 문자열 표현


2. `LogType` Enum
   로그 유형을 정의하는 Enum입니다. 이 값들은 ELK와 Kibana에서 로그를 필터링하는 데 사용됩니다.

주요 값:
* `EXECUTION_TIME`: 실행 시간 로그
* `EXECUTION_DETAIL`: 실행 상세 로그
* `EXCEPTION`: 예외 로그
* `DATABASE_QUERY`: 데이터베이스 쿼리 로그
* `INBOUND_REQUEST`: 인바운드 요청 로그
* `INBOUND_RESPONSE`: 인바운드 응답 로그
* `OUTBOUND_REQUEST`: 아웃바운드 요청 로그
* `OUTBOUND_RESPONSE`: 아웃바운드 응답 로그
* `BATCH_JOB`: 배치 작업 로그


각 `LogType`에 따른 구현체

각 로그 유형(`LogType`)에 대해 별도의 구현체가 존재합니다. 예를 들어, `EXECUTION_TIME` 로그는 실행 시간을 기록하고, `EXCEPTION` 로그는 예외 발생 정보를 기록합니다.
이 구현체들은 `LogEntry` 인터페이스를 구현하며, 각 로그 유형에 맞는 데이터와 정보를 기록합니다.

3. `LogContextScope`

스레드로컬 누수를 방지하기 위해, `LogContextScope` 클래스를 사용하여 로그 컨텍스트를 관리하는 것을 추천합니다. 이 클래스는 `AutoCloseable`을 구현하여 `try-with-resources` 구문을 사용하여 요청이 끝날 때 자동으로 flush()를 호출하고, 로그를 정리합니다. 이를 통해 로그 컨텍스트가 정확하게 처리되며, 스레드로컬 누수를 방지할 수 있습니다.

* 사용 예시:
```java
try (LogContextScope logContextScope = new LogContextScope()) {
// 요청 처리 로직
}
```

위와 같이` try-with-resources` 구문을 사용하면, 요청이 끝날 때 자동으로 `flush()`가 호출되고, 로그 컨텍스트가 정리됩니다.
이 방법을 통해 로그 컨텍스트 누수를 방지하고, 효율적인 자원 관리를 할 수 있습니다.


## `LogEntryContext` 및 `LogEntryContextManager`

1. `LogEntryContext`
   `LogEntryContext` 요청 단위로 로그를 저장하고 관리하는 클래스입니다. `InheritableThreadLocal`을 사용하여 부모 스레드에서 자식 스레드로 로그 컨텍스트를 전파할 수 있습니다. 이 클래스는 각 스레드에서 발생한 로그를 관리하며, 요청이 끝날 때 로그를 플러시하고 컨텍스트를 정리하는 기능을 제공합니다.

주요 기능:
* 로그 추가 및 조회
* 예외 발생 여부 확인
* 로그 컨텍스트 초기화 및 정리
* 자식 스레드로 로그 컨텍스트 전파 (스레드 로컬 복사)

2. `LogEntryContextManager`
   `LogEntryContextManager`는 `LogEntryContext`를 관리하는 매니저 클래스입니다. 외부에서 직접 `LogEntryContext`를 조작할 수 없으며, 이 클래스를 통해 로그를 추가하고 관리합니다. 로그의 개수가 일정 수치를 초과하면 기존 로그를 플러시하고 새로운 로그를 추가합니다. 또한, 예외 발생 시 예외 로그를 추가하고 이를 로깅합니다.

주요 기능:
* 로그 추가 및 플러시
* 스레드 간 로그 컨텍스트 전파 (부모 -> 자식 스레드)
* 예외 로그 처리
* 로그 알림 (LogNotifier)
`LogEntryContext`는 스레드로컬에 저장되며, `propagateToChildThread` 메서드를 통해 부모 스레드의 로그 컨텍스트를 자식 스레드로 전파할 수 있습니다.  이때, `ThreadContextPropagator` 클래스를 사용하여 스레드 컨텍스트 복사를 더 간편하게 처리할 수 있습니다.


3. `ThreadContextPropagator`
   `ThreadContextPropagator` 클래스는 새로운 스레드가 생성될 때 부모 스레드의 컨텍스트를 자동으로 복사하여 유지하는 기능을 제공합니다. `runWithContextRunnable`과 `runWithContextCallable` 메서드를 사용하면, 스레드 실행 시 로그 컨텍스트를 복사하여 유지하면서 작업을 수행할 수 있습니다. 이를 통해 스레드 간 로그 상태 전파를 더 쉽게 관리할 수 있습니다.

주요 기능:
* 부모 스레드의 컨텍스트를 자식 스레드로 자동으로 복사
* `Runnable`과 `Callable` 작업을 감싸서 실행
* 이를 통해, `LogEntryContext`의 스레드로컬 복사를 손쉽게 처리할 수 있어 멀티스레딩 환경에서도 로그 상태를 일관되게 유지할 수 있습니다.


## Query 캡쳐 및 로깅 (`LoggingPreparedStatement`, `SqlParameterHolder`, `QueryLoggingService를`)

쿼리 리스너는 SQL 실행을 감시하고, 실행된 SQL 쿼리의 성능, 파라미터, 실행 시간 등을 추적하여 로깅과 메트릭 수집을 수행하는 기능을 합니다.
이 기능을 구현하기 위해, `PreparedStatement`를 감싸는 `LoggingPreparedStatement` 클래스와 `SqlParameterHolder`, `QueryLoggingService`를 사용하여 SQL 쿼리 실행과 관련된 정보를 로깅하고 모니터링합니다.

1. `PreparedStatementWrapper` 클래스
   PreparedStatementWrapper는 PreparedStatement의 모든 메서드를 위임하는 래퍼 클래스입니다. 이 클래스는 PreparedStatement 객체를 감싸고, 해당 객체의 메서드를 호출할 때, 기능을 추가할 수 있는 구조로 설계되어 있습니다.

```java
public class PreparedStatementWrapper implements PreparedStatement {

    protected final PreparedStatement delegate;

    public PreparedStatementWrapper(PreparedStatement delegate) {
        this.delegate = delegate;
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return delegate.executeQuery();
    }

    // 나머지 PreparedStatement 메서드들
}

```



2. `LoggingPreparedStatement` 클래스
`LoggingPreparedStatement`는 `PreparedStatementWrapper`를 상속받아 SQL 실행을 감시하는 기능을 추가합니다.
각 SQL 실행 메서드 (executeQuery, executeUpdate, execute)를 오버라이드하여 실행 시간을 기록하고, 실행된 SQL 쿼리, 파라미터 값, 영향을 받은 행 수 등을 로깅합니다.
특히, 실행 시간이 오래 걸린 쿼리를 `QueryLoggingService.logQuery` 메서드를 통해 로깅하고, SQL 실행에 사용된 파라미터는 `SqlParameterHolder.addParameter`로 추적하여, 쿼리 실행 후 `SqlParameterHolder.clear`로 파라미터를 정리합니다.


3. `SqlParameterHolder` 클래스
`SqlParameterHolder`는 SQL 쿼리에 바인딩된 파라미터들을 `ThreadLocal`을 이용해 관리하는 클래스입니다.
멀티스레드 환경에서도 각 스레드별로 안전하게 파라미터를 저장하고, 쿼리 실행이 끝난 후에는 clear 메서드를 호출하여 `ThreadLocal`을 정리합니다. 
이렇게 하지 않으면 스레드 로컬에 쌓인 파라미터들이 메모리 누수의 원인이 될 수 있습니다.

4. `QueryLoggingService` 클래스
`QueryLoggingService`는 SQL 쿼리 실행에 대한 정보를 로깅하는 서비스입니다.
`logQuery` 메서드는 SQL, 실행 시간, 영향을 받은 행 수 등을 기록하고, `QueryPerformanceEvaluator`를 사용하여 쿼리 실행 시간에 따라 로깅 레벨을 결정합니다. 
또한, `MetricCollectorProvider`를 통해 쿼리 메트릭을 수집하여 성능을 모니터링합니다.