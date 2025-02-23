package com.monikit.core;

public interface QueryLoggingService {

    void logQuery(String traceId, String sql, String parameter, long executionTime, int rowsAffected);

}
