package com.monikit.starter.webflux.filter;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;

import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogContextScope;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.webflux.ExcludePathConstant;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import reactor.core.publisher.Mono;

/**
 * 요청 단위로 LogContextScope를 관리하는 게이트웨이 필터.
 * <p>
 * - 요청이 시작될 때 LogContextScope를 생성하여 자동으로 컨텍스트를 초기화함.
 * - 요청이 끝날 때 자동으로 LogContextScope를 종료하여 컨텍스트를 정리함.
 * - try-with-resources 구문을 활용하여 안전하게 관리됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
public class LogContextScopeGatewayFilter implements GatewayFilter {

    private final LogEntryContextManager logEntryContextManager;
    private final TraceIdProvider traceIdProvider;

    public LogContextScopeGatewayFilter(LogEntryContextManager logEntryContextManager, TraceIdProvider traceIdProvider) {
        this.logEntryContextManager = logEntryContextManager;
        this.traceIdProvider = traceIdProvider;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestUri = exchange.getRequest().getURI().getPath();

        if (ExcludePathConstant.EXCLUDED_PATHS.contains(requestUri)) {
            return chain.filter(exchange);
        }

        Instant start = Instant.now();
        try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
            String traceId = traceIdProvider.getTraceId();
            logRequest(exchange, traceId);

            return chain.filter(exchange)
                .doOnTerminate(() -> {
                    long executionTime = Instant.now().toEpochMilli() - start.toEpochMilli();
                    logResponse(exchange, traceId, executionTime);
                });
        }
    }

    private void logRequest(ServerWebExchange exchange, String traceId) {
        ServerHttpRequest request = exchange.getRequest();
        String method = request.getMethod().name();
        String uri = request.getURI().getPath();
        String query = request.getURI().getQuery();
        Map<String, String> headers = extractHeaders(request.getHeaders());
        String body = exchange.getAttribute("cachedRequestBody");
        InetSocketAddress remoteAddress = request.getRemoteAddress();

        logEntryContextManager.addLog(HttpInboundRequestLog.create(
            traceId,
            LogLevel.INFO,
            uri,
            method,
            query,
            body != null ? body : "[EMPTY]",
            headers,
            remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown",
            request.getHeaders().getFirst("User-Agent")
        ));
    }

    private void logResponse(ServerWebExchange exchange, String traceId, long executionTime) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String method = request.getMethod().name();
        String uri = request.getURI().getPath();
        int status = response.getStatusCode() != null ? response.getStatusCode().value() : 0;
        Map<String, String> headers = extractHeaders(response.getHeaders());
        String body = exchange.getAttribute("cachedResponseBody");

        logEntryContextManager.addLog(HttpInboundResponseLog.create(
            traceId,
            LogLevel.INFO,
            method,
            uri,
            status,
            headers,
            body != null ? body : "[EMPTY]",
            executionTime
        ));
    }



    /**
     * 헤더를 추출하여 Map 형태로 반환한다.
     *
     * @param httpHeaders HTTP 헤더객체
     * @return 응답 헤더를 Map<String, String>으로 반환
     */

    private Map<String, String> extractHeaders(HttpHeaders httpHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();

        for (Map.Entry<String, List<String>> entry : httpHeaders.entrySet()) {
            String headerName = entry.getKey();
            List<String> values = entry.getValue();
            if (values != null && !values.isEmpty()) {
                headers.put(headerName, values.getFirst());
            }
        }

        return headers;
    }




}