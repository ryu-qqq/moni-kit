package com.monikit.starter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.monikit.core.HttpOutboundRequestLog;
import com.monikit.core.HttpOutboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;


import feign.Logger;
import feign.Request;
import feign.Response;

/**
 * Feign Logger 확장하여 요청 및 응답을 자동으로 로깅하는 클래스.
 *
 * @author ryu-qqq
 * @since 1.0
 */

public class FeignLogger extends Logger {

    @Override
    protected void log(String s, String s1, Object... objects) {

    }

    @Override
    protected void logRequest(String configKey, Level logLevel, Request request) {
        long startTime = System.currentTimeMillis();
        String traceId = TraceIdProvider.currentTraceId();

        HttpOutboundRequestLog requestLog = HttpOutboundRequestLog.create(
            traceId,
            request.httpMethod().name(),
            request.url(),
            request.headers().toString(),
            request.body() != null ? new String(request.body(), StandardCharsets.UTF_8) : "",
            startTime,
            LogLevel.INFO
        );

        LogEntryContextManager.addLog(requestLog);
    }

    @Override
    protected Response logAndRebufferResponse(String configKey, Level logLevel, Response response, long elapsedTime)
        throws IOException {
        String traceId = TraceIdProvider.currentTraceId();

        String responseBody = response.body() != null
            ? new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8)
            : "";

        HttpOutboundResponseLog responseLog = HttpOutboundResponseLog.create(
            traceId,
            response.request().url(),
            response.status(),
            response.headers().toString(),
            responseBody,
            elapsedTime,
            response.status() >= 500 ? LogLevel.ERROR : LogLevel.WARN
        );

        LogEntryContextManager.addLog(responseLog);
        return response.toBuilder()
            .body(responseBody.getBytes(StandardCharsets.UTF_8))
            .build();
    }
}