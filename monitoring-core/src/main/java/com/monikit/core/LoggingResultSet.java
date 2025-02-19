package com.monikit.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * `LoggingPreparedStatement`에서 반환하는 `ResultSet`을 감싸서,
 * `close()` 시점에 SQL 실행 정보를 로깅하고 `SqlParameterHolder.clear()`를 호출하는 클래스.
 */
public class LoggingResultSet extends ResultSetWrapper {

    private final String sql;
    private final long startTime;

    public LoggingResultSet(ResultSet delegate, String sql, long startTime) {
        super(delegate);
        this.sql = sql;
        this.startTime = startTime;
    }

    @Override
    public void close() throws SQLException {
        try {
            super.close();
        } finally {
            QueryLoggingService.logQuery(sql, System.currentTimeMillis() - startTime, -1);
            SqlParameterHolder.clear();
        }
    }
}
