package com.monikit.starter.interceptor;

import java.io.IOException;
import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.starter.TraceIdProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@DisplayName("HttpLoggingInterceptor 테스트")
class HttpLoggingInterceptorTest {

    private LogEntryContextManager mockLogEntryContextManager;
    private HttpLoggingInterceptor interceptor;

    @BeforeEach
    void setUp() {
        mockLogEntryContextManager = mock(LogEntryContextManager.class);
        interceptor = new HttpLoggingInterceptor(mockLogEntryContextManager);
    }

    @Nested
    @DisplayName("preHandle() 테스트")
    class PreHandleTests {

        @Test
        @DisplayName("HTTP 요청이 정상적으로 로깅되어야 한다")
        void shouldLogHttpRequest() {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/test");
            request.setQueryString("param1=value1");
            request.addHeader("User-Agent", "TestAgent");
            request.setRemoteAddr("127.0.0.1");

            MockHttpServletResponse response = new MockHttpServletResponse();

            // When
            boolean result = interceptor.preHandle(request, response, new Object());

            // Then
            assertTrue(result);
            ArgumentCaptor<HttpInboundRequestLog> captor = ArgumentCaptor.forClass(HttpInboundRequestLog.class);
            verify(mockLogEntryContextManager, times(1)).addLog(captor.capture());

            HttpInboundRequestLog log = captor.getValue();
            assertEquals("POST", log.getHttpMethod());
            assertEquals("/api/test", log.getRequestUri());
            assertEquals("param1=value1", log.getQueryParams());
            assertEquals("TestAgent", log.getUserAgent());
            assertEquals("127.0.0.1", log.getClientIp());
        }
    }

    @Nested
    @DisplayName("afterCompletion() 테스트")
    class AfterCompletionTests {

        @Test
        @DisplayName("HTTP 응답이 정상적으로 로깅되어야 한다")
        void shouldLogHttpResponse() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);
            response.addHeader("Content-Type", "application/json");

            // 요청이 들어올 때 preHandle() 먼저 호출해야 afterCompletion()에서 실행 시간을 측정 가능
            interceptor.preHandle(request, response, new Object());

            // When
            interceptor.afterCompletion(request, response, new Object(), null);

            // Then
            ArgumentCaptor<HttpInboundResponseLog> captor = ArgumentCaptor.forClass(HttpInboundResponseLog.class);
            verify(mockLogEntryContextManager, times(1)).addLog(captor.capture());

            HttpInboundResponseLog log = captor.getValue();
            assertEquals("GET", log.getHttpMethod());
            assertEquals("/api/test", log.getRequestUri());
            assertEquals(200, log.getStatusCode());
            assertTrue(log.getHeaders().contains("Content-Type"));
        }

        @Test
        @DisplayName("HTTP 응답 시간이 정상적으로 측정되어야 한다")
        void shouldCaptureExecutionTime() throws IOException {
            // Given
            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/time");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);

            interceptor.preHandle(request, response, new Object());

            // 실행 시간 테스트를 위해 약간의 지연 추가
            Instant beforeExecution = Instant.now();
            try {
                Thread.sleep(50); // 50ms 지연
            } catch (InterruptedException ignored) {}

            // When
            interceptor.afterCompletion(request, response, new Object(), null);
            Instant afterExecution = Instant.now();

            // Then
            ArgumentCaptor<HttpInboundResponseLog> captor = ArgumentCaptor.forClass(HttpInboundResponseLog.class);
            verify(mockLogEntryContextManager, times(1)).addLog(captor.capture());

            HttpInboundResponseLog log = captor.getValue();
            long executionTime = log.getExecutionTime();
            assertTrue(executionTime >= 50, "Execution time should be greater than or equal to 50ms");
            assertTrue(executionTime <= (afterExecution.toEpochMilli() - beforeExecution.toEpochMilli()) + 10);
        }
    }

    @Nested
    @DisplayName("TraceId 테스트")
    class TraceIdTests {


        @AfterEach
        void cleanup() {
            TraceIdProvider.clear();
        }


        @Test
        @DisplayName("TraceId가 요청 및 응답에 올바르게 적용되어야 한다")
        void shouldApplyTraceIdToRequestAndResponseLogs() throws IOException {
            // Given
            String expectedTraceId = "trace-id-1234";
            TraceIdProvider.setTraceId(expectedTraceId);

            MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/trace");
            MockHttpServletResponse response = new MockHttpServletResponse();
            response.setStatus(200);

            // When
            interceptor.preHandle(request, response, new Object());
            interceptor.afterCompletion(request, response, new Object(), null);

            // Then
            ArgumentCaptor<HttpInboundRequestLog> requestCaptor = ArgumentCaptor.forClass(HttpInboundRequestLog.class);
            ArgumentCaptor<HttpInboundResponseLog> responseCaptor = ArgumentCaptor.forClass(HttpInboundResponseLog.class);

            verify(mockLogEntryContextManager, times(1)).addLog(requestCaptor.capture());
            verify(mockLogEntryContextManager, times(1)).addLog(responseCaptor.capture());

            HttpInboundRequestLog requestLog = requestCaptor.getValue();
            HttpInboundResponseLog responseLog = responseCaptor.getValue();

            assertEquals(expectedTraceId, requestLog.getTraceId());
            assertEquals(expectedTraceId, responseLog.getTraceId());
        }
    }
}