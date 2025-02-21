package com.monikit.core;

public interface QueryLoggingService {

    void logQuery(String sql, long executionTime, int rowsAffected);

}
