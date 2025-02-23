package com.monikit.starter;

import feign.Logger;
import feign.Request;
import feign.Response;
import feign.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.monikit.core.HttpOutboundRequestLog;
import com.monikit.core.HttpOutboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

@DisplayName("FeignLogger 테스트")
@ExtendWith(MockitoExtension.class)
class FeignLoggerTest  {

    @Mock
    private LogEntryContextManager mockLogEntryContextManager;

    @InjectMocks
    private FeignLogger feignLogger;

    @Nested
    @DisplayName("요청 로깅 테스트")
    class RequestLoggingTests {

        @Test
        @DisplayName("Feign 요청이 로깅되어야 한다.")
        void shouldLogFeignRequest() {
            // Given
            Request request = Request.create(
                Request.HttpMethod.GET,
                "http://example.com/api",
                Map.of("Authorization", Collections.singletonList("Bearer token")),
                "requestBody".getBytes(StandardCharsets.UTF_8),
                StandardCharsets.UTF_8,
                null
            );

            // When
            feignLogger.logRequest("testConfigKey", Logger.Level.FULL, request);

            // Then
            ArgumentCaptor<HttpOutboundRequestLog> logCaptor = ArgumentCaptor.forClass(HttpOutboundRequestLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            HttpOutboundRequestLog loggedRequest = logCaptor.getValue();
            assertEquals("http://example.com/api", loggedRequest.getTargetUrl());
            assertEquals("GET", loggedRequest.getHttpMethod());
            assertEquals("requestBody", loggedRequest.getRequestBody());
            assertEquals(LogLevel.INFO, loggedRequest.getLogLevel());
        }
    }

    @Nested
    @DisplayName("응답 로깅 테스트")
    class ResponseLoggingTests {

        @Test
        @DisplayName("Feign 응답이 로깅되어야 한다.")
        void shouldLogFeignResponse() throws IOException {
            // Given
            Request request = Request.create(
                Request.HttpMethod.GET,
                "http://example.com/api",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
            );

            Response response = Response.builder()
                .request(request)
                .status(200)
                .headers(Map.of("Content-Type", Collections.singletonList("application/json")))
                .body("{\"message\": \"success\"}", StandardCharsets.UTF_8)
                .build();

            // When
            Response rebufferedResponse = feignLogger.logAndRebufferResponse("testConfigKey", Logger.Level.FULL, response, 150L);

            // Then
            ArgumentCaptor<HttpOutboundResponseLog> logCaptor = ArgumentCaptor.forClass(HttpOutboundResponseLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            HttpOutboundResponseLog loggedResponse = logCaptor.getValue();
            assertEquals("http://example.com/api", loggedResponse.getTargetUrl());
            assertEquals(200, loggedResponse.getStatusCode());
            assertEquals("{\"message\": \"success\"}", loggedResponse.getResponseBody());
            assertEquals(LogLevel.WARN, loggedResponse.getLogLevel()); // 200~499는 WARN
            assertEquals(150L, loggedResponse.getExecutionTime());

            // 응답이 재사용 가능하게 변환되었는지 확인
            assertNotNull(rebufferedResponse.body());
            assertEquals("{\"message\": \"success\"}", Util.toString(rebufferedResponse.body().asReader(StandardCharsets.UTF_8)));
        }

        @Test
        @DisplayName("5xx 응답일 경우 ERROR 로그 레벨로 기록해야 한다.")
        void shouldLogErrorLevelFor5xxResponses() throws IOException {
            // Given
            Request request = Request.create(
                Request.HttpMethod.POST,
                "http://example.com/api",
                Map.of(),
                null,
                StandardCharsets.UTF_8,
                null
            );

            Response response = Response.builder()
                .request(request)
                .status(500)
                .headers(Map.of())
                .body("Internal Server Error", StandardCharsets.UTF_8)
                .build();

            // When
            feignLogger.logAndRebufferResponse("testConfigKey", Logger.Level.FULL, response, 200L);

            // Then
            ArgumentCaptor<HttpOutboundResponseLog> logCaptor = ArgumentCaptor.forClass(HttpOutboundResponseLog.class);
            verify(mockLogEntryContextManager).addLog(logCaptor.capture());

            HttpOutboundResponseLog loggedResponse = logCaptor.getValue();
            assertEquals("http://example.com/api", loggedResponse.getTargetUrl());
            assertEquals(500, loggedResponse.getStatusCode());
            assertEquals("Internal Server Error", loggedResponse.getResponseBody());
            assertEquals(LogLevel.ERROR, loggedResponse.getLogLevel()); // 500 이상은 ERROR
        }
    }
}