package com.monikit.starter.web.filter;

import java.io.IOException;
import java.util.UUID;

import org.springframework.web.filter.OncePerRequestFilter;

import com.monikit.core.TraceIdProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 요청 단위로 Trace ID를 자동으로 설정하는 필터.
 * <p>
 * - 요청 헤더에 {@code X-Trace-Id}가 없으면 자동으로 UUID 기반 Trace ID를 생성하여 설정함.
 * - 생성된 Trace ID를 {@link com.monikit.core.TraceIdProvider}를 통해 설정하고, 로그 기록 시 자동으로 포함될 수 있도록 지원함.
 * - 응답 헤더에도 {@code X-Trace-Id}를 추가하여 클라이언트가 추적 가능하도록 함.
 * - 요청 종료 시 {@link TraceIdProvider#clear()}를 호출하여 MDC를 정리하고, 메모리 누수를 방지함.
 * </p>
 * <p>
 * 예시:
 * <pre>{@code
 * // 요청에 TraceId가 없으면 자동 생성되고, 응답에 추가됨
 * }</pre>
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 * @see com.monikit.core.TraceIdProvider
 */

public class TraceIdFilter extends OncePerRequestFilter {

    private final TraceIdProvider traceIdProvider;

    private static final String TRACE_ID_HEADER = "X-Trace-Id";

    /**
     * {@link TraceIdFilter}의 생성자.
     *
     * @param traceIdProvider {@link TraceIdProvider} 인스턴스
     */
    public TraceIdFilter(TraceIdProvider traceIdProvider) {
        this.traceIdProvider = traceIdProvider;
    }

    /**
     * HTTP 요청에 대해 Trace ID를 설정하고, 응답에 Trace ID를 추가하는 필터링 작업을 수행한다.
     * <p>
     * - 요청 헤더에 {@code X-Trace-Id}가 없으면 UUID 기반의 새로운 Trace ID를 생성하여 요청 및 응답 헤더에 추가함.
     * - 요청이 종료된 후 {@link TraceIdProvider#clear()}를 호출하여 MDC에서 해당 Trace ID를 정리함.
     * </p>
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException IO 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID_HEADER);

        if (traceId == null || traceId.isEmpty()) {
            traceId = UUID.randomUUID().toString();
        }

        // Trace ID 설정
        traceIdProvider.setTraceId(traceId);

        // 응답 헤더에 Trace ID 추가
        response.setHeader(TRACE_ID_HEADER, traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 요청 종료 후 Trace ID 정리
            traceIdProvider.clear();
        }
    }

}