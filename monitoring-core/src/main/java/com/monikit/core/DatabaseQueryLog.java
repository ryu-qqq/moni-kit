package com.monikit.core;

import java.util.Map;
import java.util.Objects;

/**
 * SQL 쿼리 실행 정보를 기록하는 로그 클래스.
 * <p>
 * 실행된 SQL 쿼리, 실행 시간, 데이터베이스 연결 정보, 바인딩된 파라미터,
 * 변경된 행 개수 및 결과 개수를 포함하여 데이터베이스 성능 분석에 활용된다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class DatabaseQueryLog extends AbstractLogEntry {
    private final String query;
    private final long executionTime;
    private final String dataSource;
    private final String parameters;
    private final int rowsAffected;
    private final int resultSize;

    protected DatabaseQueryLog(String traceId, String query, long executionTime, String dataSource,
                               String parameters, int rowsAffected, int resultSize, LogLevel logLevel) {
        super(traceId, logLevel);
        this.query = query;
        this.executionTime = executionTime;
        this.dataSource = dataSource;
        this.parameters = parameters;
        this.rowsAffected = rowsAffected;
        this.resultSize = resultSize;
    }

    @Override
    public LogType getLogType() {
        return LogType.DATABASE_QUERY;
    }

    @Override
    protected void addExtraFields(Map<String, Object> logMap) {
        logMap.put("query", query);
        logMap.put("executionTime", executionTime + "ms");
        logMap.put("dataSource", dataSource);
        logMap.put("parameters", parameters);
        logMap.put("rowsAffected", rowsAffected);
        logMap.put("resultSize", resultSize);
    }

    public static DatabaseQueryLog create(String traceId, String query, long executionTime, String dataSource,
                                          String parameters, int rowsAffected, int resultSize, LogLevel logLevel) {
        return new DatabaseQueryLog(traceId, query, executionTime, dataSource, parameters, rowsAffected, resultSize, logLevel);
    }

    @Override
    public boolean equals(Object object) {
        if (this
            == object) return true;
        if (object
            == null
            || getClass()
            != object.getClass()) return false;
        DatabaseQueryLog that = (DatabaseQueryLog) object;
        return executionTime
            == that.executionTime
            && rowsAffected
            == that.rowsAffected
            && resultSize
            == that.resultSize
            && Objects.equals(query, that.query)
            && Objects.equals(dataSource, that.dataSource)
            && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, executionTime, dataSource, parameters, rowsAffected, resultSize);
    }

    @Override
    public String toString() {
        return "DatabaseQueryLog{"
            +
            "query='"
            + query
            + '\''
            +
            ", executionTime="
            + executionTime
            +
            ", dataSource='"
            + dataSource
            + '\''
            +
            ", parameters='"
            + parameters
            + '\''
            +
            ", rowsAffected="
            + rowsAffected
            +
            ", resultSize="
            + resultSize
            +
            '}';
    }
}
