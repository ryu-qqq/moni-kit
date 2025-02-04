package com.monikit.starter.interceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.filter.RequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 요청 및 응답을 자동으로 로깅하는 인터셉터.
 * <p>
 * - 요청이 들어올 때 HttpInboundRequestLog를 생성하여 로그를 남김.
 * - 응답이 나갈 때 HttpInboundResponseLog를 생성하여 로그를 남김.
 * - TraceId를 자동으로 관리함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Instant> requestStartTime = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = TraceIdProvider.currentTraceId();
        requestStartTime.set(Instant.now());

        LogEntryContextManager.addLog(HttpInboundRequestLog.create(
            traceId,
            request.getMethod(),
            request.getRequestURI(),
            request.getQueryString(),
            extractHeaders(request),
            extractRequestBody(request),
            request.getRemoteAddr(),
            request.getHeader("User-Agent"),
            LogLevel.INFO
        ));

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws
        IOException {
        String traceId = TraceIdProvider.currentTraceId();
        Instant startTime = requestStartTime.get();
        long executionTime = startTime != null ? Instant.now().toEpochMilli() - startTime.toEpochMilli() : 0;

        LogEntryContextManager.addLog(HttpInboundResponseLog.create(
            traceId,
            request.getMethod(),
            request.getRequestURI(),
            response.getStatus(),
            extractHeaders(response),
            extractResponseBody(response),
            executionTime,
            LogLevel.INFO
        ));

        requestStartTime.remove();
    }

    private String extractHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("; ");
        }
        return headers.toString();
    }

    private String extractHeaders(HttpServletResponse response) {
        StringBuilder headers = new StringBuilder();
        for (String headerName : response.getHeaderNames()) {
            headers.append(headerName).append(": ").append(response.getHeader(headerName)).append("; ");
        }
        return headers.toString();
    }

    private String extractRequestBody(HttpServletRequest request) {
        if (request instanceof RequestWrapper) {
            byte[] requestBody = ((RequestWrapper) request).getContentAsByteArray();
            return new String(requestBody);
        }
        return "RequestBody Can't read";
    }

    private String extractResponseBody(HttpServletResponse response) throws IOException {
        if (response instanceof ContentCachingResponseWrapper wrappedResponse) {
            byte[] contentAsByteArray = wrappedResponse.getContentAsByteArray();
            wrappedResponse.copyBodyToResponse();
            return new String(contentAsByteArray);
        }
        return "ResponseBody Can't read";
    }

}