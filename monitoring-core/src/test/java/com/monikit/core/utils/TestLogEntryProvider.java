package com.monikit.core.utils;

import java.time.Instant;
import java.util.Map;

import com.monikit.core.*;
import com.monikit.core.BatchJobLog;
import com.monikit.core.DatabaseQueryLog;
import com.monikit.core.ExceptionLog;
import com.monikit.core.ExecutionDetailLog;
import com.monikit.core.ExecutionLog;
import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.HttpOutboundRequestLog;
import com.monikit.core.HttpOutboundResponseLog;

/**
 * `TestLogEntryProvider`의 테스트 데이터 제공 클래스.
 * <p>
 * 테스트에서 일관된 데이터를 사용하여 예측 가능한 결과를 보장한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public class TestLogEntryProvider {

    private static final String TRACE_ID = "test-trace-123";
    private static final LogLevel LOG_LEVEL = LogLevel.INFO;
    private static final String CLASS_NAME = "TestService";
    private static final String METHOD_NAME = "testMethod";
    private static final String QUERY = "SELECT * FROM test_table";
    private static final long EXECUTION_TIME = 200;
    private static final String DATA_SOURCE = "test-db";
    private static final String TABLE_NAME = "test_table";
    private static final String PARAMETERS = "{}";
    private static final int ROWS_AFFECTED = 5;
    private static final int RESULT_SIZE = 10;
    private static final String HTTP_METHOD = "POST";
    private static final String URI = "/api/test";
    private static final String QUERY_PARAMS = "?id=1";
    private static final Map<String, String> HEADERS = Map.of("Authorization","Bearer test-token");
    private static final String REQUEST_BODY = "{\"name\":\"test\"}";
    private static final String CLIENT_IP = "127.0.0.1";
    private static final String USER_AGENT = "TestAgent/1.0";
    private static final int STATUS_CODE = 200;
    private static final String RESPONSE_BODY = "{\"message\":\"success\"}";
    private static final String BATCH_JOB_NAME = "test-batch-job";
    private static final Instant START_TIME = Instant.now();
    private static final Instant END_TIME = START_TIME.plusMillis(500);
    private static final String STATUS = "SUCCESS";
    private static final String ERROR_MESSAGE = "";
    private static final String TARGET_URL = "https://api.example.com/test";
    private static final String INPUT_PARAMS = "{\"input\": \"test-value\"}";
    private static final String OUTPUT_VALUE = "{\"output\": \"result-value\"}";

    public static ExecutionLog executionTimeLog() {
        return ExecutionLog.create(TRACE_ID, CLASS_NAME, METHOD_NAME, EXECUTION_TIME);
    }

    public static DatabaseQueryLog databaseQueryLog() {
        return DatabaseQueryLog.create(TRACE_ID, QUERY, EXECUTION_TIME, DATA_SOURCE, TABLE_NAME, ROWS_AFFECTED, RESULT_SIZE, LOG_LEVEL);
    }

    public static ExceptionLog exceptionLog() {
        return ExceptionLog.create(TRACE_ID, new RuntimeException("Test Exception"));
    }

    public static HttpInboundRequestLog httpInboundRequestLog() {
        return HttpInboundRequestLog.create(TRACE_ID, LOG_LEVEL, URI, HTTP_METHOD, QUERY_PARAMS, REQUEST_BODY, HEADERS, CLIENT_IP, USER_AGENT);
    }

    public static HttpInboundResponseLog httpInboundResponseLog() {
        return HttpInboundResponseLog.create(TRACE_ID, LOG_LEVEL, HTTP_METHOD, TARGET_URL, STATUS_CODE, HEADERS, RESPONSE_BODY, EXECUTION_TIME);
    }

    public static BatchJobLog batchJobLog() {
        return BatchJobLog.create(TRACE_ID, BATCH_JOB_NAME, START_TIME, END_TIME, EXECUTION_TIME, STATUS, "SUCCESS", ERROR_MESSAGE, LOG_LEVEL);
    }

    public static HttpOutboundRequestLog httpOutboundRequestLog() {
        return HttpOutboundRequestLog.create(TRACE_ID, LOG_LEVEL, TARGET_URL, HTTP_METHOD, HEADERS, QUERY_PARAMS, REQUEST_BODY);
    }

    public static HttpOutboundResponseLog httpOutboundResponseLog() {
        return HttpOutboundResponseLog.create(TRACE_ID, LOG_LEVEL, HTTP_METHOD, TARGET_URL, STATUS_CODE, HEADERS, RESPONSE_BODY, EXECUTION_TIME);
    }

    public static ExecutionDetailLog executionDetailLog() {
        return ExecutionDetailLog.create(TRACE_ID, CLASS_NAME, METHOD_NAME, EXECUTION_TIME, INPUT_PARAMS, OUTPUT_VALUE, 1000);
    }


}