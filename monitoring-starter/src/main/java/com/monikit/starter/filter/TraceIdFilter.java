package com.monikit.starter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

/**
 * 요청 단위로 Trace ID를 자동으로 설정하는 필터.
 * <p>
 * - 요청 헤더에 `X-Trace-Id`가 없으면 자동으로 UUID 기반 Trace ID를 생성함.
 * - 생성된 Trace ID를 MDC에 저장하여 로깅 시 자동으로 포함될 수 있도록 지원함.
 * - 응답 헤더에도 `X-Trace-Id`를 추가하여 클라이언트가 추적 가능하도록 함.
 * - 요청 종료 시 MDC를 정리하여 메모리 누수를 방지함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_ID_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);

        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        MDC.put(MDC_TRACE_ID_KEY, traceId);

        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

}