package com.monikit.starter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import com.monikit.core.HttpOutboundRequestLog;
import com.monikit.core.HttpOutboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;

import feign.Logger;
import feign.Request;
import feign.Response;

/**
 * Feign Logger 확장하여 요청 및 응답을 자동으로 로깅하는 클래스.
 * <p>
 * - FeignClient에서 발생하는 모든 HTTP 요청 및 응답을 로깅.
 * - 요청 헤더, 본문, 응답 코드 및 본문을 기록하여 분석 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */
@Component
public class FeignLogger extends Logger {

    private final LogEntryContextManager logEntryContextManager;

    public FeignLogger(LogEntryContextManager logEntryContextManager) {
        this.logEntryContextManager = logEntryContextManager;
    }

    @Override
    protected void log(String s, String s1, Object... objects) {

    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        long startTime = System.currentTimeMillis();
        String traceId = TraceIdProvider.getTraceId();

        String requestBody = request.body() != null ? new String(request.body(), StandardCharsets.UTF_8) : "";

        HttpOutboundRequestLog requestLog = HttpOutboundRequestLog.create(
            traceId,
            request.httpMethod().name(),
            request.url(),
            formatHeaders(request.headers()),
            requestBody,
            startTime,
            LogLevel.INFO
        );

        logEntryContextManager.addLog(requestLog);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
        throws IOException {
        String traceId = TraceIdProvider.getTraceId();

        String responseBody = response.body() != null
            ? new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8)
            : "";

        HttpOutboundResponseLog responseLog = HttpOutboundResponseLog.create(
            traceId,
            response.request().url(),
            response.status(),
            formatHeaders(response.headers()),
            responseBody,
            elapsedTime,
            response.status() >= 500 ? LogLevel.ERROR : LogLevel.WARN
        );

        logEntryContextManager.addLog(responseLog);

        return response.toBuilder()
            .body(responseBody.getBytes(StandardCharsets.UTF_8))
            .build();
    }

    private String formatHeaders(Object headers) {
        return headers != null ? headers.toString() : "No Headers";
    }

}