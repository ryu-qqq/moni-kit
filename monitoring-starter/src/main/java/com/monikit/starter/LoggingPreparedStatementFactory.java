package com.monikit.starter;

import java.sql.PreparedStatement;

import org.springframework.stereotype.Component;

import com.monikit.core.LoggingPreparedStatement;
import com.monikit.core.QueryLoggingService;
import com.monikit.core.SqlParameterHolder;

/**
 * `LoggingPreparedStatement`를 생성하는 팩토리.
 * <p>
 * - `QueryLoggingService`를 주입받아 `LoggingPreparedStatement`를 생성.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */
@Component
public class LoggingPreparedStatementFactory {

    private final QueryLoggingService queryLoggingService;

    public LoggingPreparedStatementFactory(QueryLoggingService queryLoggingService) {
        this.queryLoggingService = queryLoggingService;
    }

    public LoggingPreparedStatement create(PreparedStatement delegate, String sql) {
        SqlParameterHolder holder = new SqlParameterHolder();
        String traceId = TraceIdProvider.getTraceId();
        return new LoggingPreparedStatement(delegate, traceId, sql, holder, queryLoggingService);
    }

}
