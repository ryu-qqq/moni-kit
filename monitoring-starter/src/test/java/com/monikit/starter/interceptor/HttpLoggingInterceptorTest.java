package com.monikit.starter.interceptor;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogEntryContext;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.MdcTraceIdProvider;
import com.monikit.starter.filter.RequestWrapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("HttpLoggingInterceptor 테스트")
class HttpLoggingInterceptorTest {

    private final HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ContentCachingResponseWrapper wrappedResponse;
    private Object handler;

    @BeforeEach
    void setup() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        wrappedResponse = new ContentCachingResponseWrapper(response);
        handler = new Object();

        LogEntryContextManager.flush();
        TraceIdProvider.setInstance(new MdcTraceIdProvider());
        LogEntryContextManager.setLogNotifier(new DefaultLogNotifier());



    }

    @Test
    @DisplayName("should log request details including body when preHandle is called")
    void shouldLogRequestDetailsIncludingBodyWhenPreHandleIsCalled() throws IOException {
        request.setMethod("POST");
        request.setRequestURI("/test");
        request.setQueryString("param=value");
        request.addHeader("User-Agent", "JUnit Test Agent");
        request.setRemoteAddr("127.0.0.1");

        String requestBody = "{ \"key\": \"value\" }";
        request.setContent(requestBody.getBytes());

        RequestWrapper wrappedRequest = new RequestWrapper(request);

        MdcTraceIdProvider.setTraceId("test-trace-id");
        boolean result = interceptor.preHandle(wrappedRequest, response, handler);

        assertTrue(result, "preHandle should return true");

        List<HttpInboundRequestLog> logs = LogEntryContext.getLogs().stream()
            .filter(log -> log instanceof HttpInboundRequestLog)
            .map(log -> (HttpInboundRequestLog) log)
            .toList();

        assertEquals(1, logs.size());
        HttpInboundRequestLog log = logs.getFirst();

        assertEquals("test-trace-id", log.getTraceId());
        assertEquals("POST", log.getHttpMethod());
        assertEquals("/test", log.getRequestUri());
        assertEquals("param=value", log.getQueryParams());
        assertEquals("JUnit Test Agent", log.getUserAgent());
        assertEquals("127.0.0.1", log.getClientIp());
        assertEquals(LogLevel.INFO, log.getLogLevel());

        assertEquals(requestBody, log.getRequestBody());
    }

    @Test
    @DisplayName("should log response details including body when afterCompletion is called")
    void shouldLogResponseDetailsIncludingBodyWhenAfterCompletionIsCalled() throws IOException {
        request.setMethod("POST");
        request.setRequestURI("/api/data");

        response.setStatus(200);
        response.setHeader("Content-Type", "application/json");

        String responseBody = "{ \"success\": true }";
        wrappedResponse.getOutputStream().write(responseBody.getBytes());
        wrappedResponse.flushBuffer();

        MdcTraceIdProvider.setTraceId("test-trace-id");
        interceptor.afterCompletion(request, wrappedResponse, handler, null);

        List<HttpInboundResponseLog> logs = LogEntryContext.getLogs().stream()
            .filter(log -> log instanceof HttpInboundResponseLog)
            .map(log -> (HttpInboundResponseLog) log)
            .toList();

        assertEquals(1, logs.size());
        HttpInboundResponseLog log = logs.getFirst();

        assertEquals("test-trace-id", log.getTraceId());
        assertEquals("POST", log.getHttpMethod());
        assertEquals("/api/data", log.getRequestUri());
        assertEquals(200, log.getStatusCode());
        assertEquals(LogLevel.INFO, log.getLogLevel());

        assertEquals(responseBody, log.getResponseBody());
    }

}