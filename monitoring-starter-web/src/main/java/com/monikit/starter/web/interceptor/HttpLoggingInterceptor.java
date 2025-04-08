package com.monikit.starter.web.interceptor;

import java.io.IOException;
import java.time.Instant;
import java.util.Enumeration;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.monikit.core.HttpInboundRequestLog;
import com.monikit.core.HttpInboundResponseLog;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogLevel;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.web.filter.RequestWrapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


/**
 * HTTP 요청 및 응답을 자동으로 로깅하는 인터셉터.
 * <p>
 * - 요청이 들어올 때 {@link HttpInboundRequestLog}를 생성하여 요청에 대한 로그를 기록함.
 * - 응답이 나갈 때 {@link HttpInboundResponseLog}를 생성하여 응답에 대한 로그를 기록함.
 * - {@link TraceIdProvider}를 사용하여 {@code TraceId}를 자동으로 관리하고 로깅 시 포함되도록 함.
 * - {@link LogEntryContextManager}를 주입받아 로그를 관리하고, 요청과 응답에 대한 로그를 기록함.
 * </p>
 * <p>
 * - 요청에 포함된 헤더, 본문, 메소드 및 URI 등과 응답의 상태 코드, 헤더 및 본문 등의 정보를 기록.
 * - {@link LogLevel#INFO} 레벨로 로그를 기록하며, 로그에 포함되는 {@code TraceId}는 전체 요청/응답을 추적 가능하게 함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 * @see HttpInboundRequestLog
 * @see HttpInboundResponseLog
 * @see TraceIdProvider
 * @see LogEntryContextManager
 */

public class HttpLoggingInterceptor implements HandlerInterceptor {

    private static final ThreadLocal<Instant> requestStartTime = new ThreadLocal<>();
    private final LogEntryContextManager logEntryContextManager;
    private final TraceIdProvider traceIdProvider;

    /**
     * {@link HttpLoggingInterceptor}의 생성자.
     *
     * @param logEntryContextManager {@link LogEntryContextManager} 인스턴스
     * @param traceIdProvider {@link TraceIdProvider} 인스턴스
     */
    public HttpLoggingInterceptor(LogEntryContextManager logEntryContextManager, TraceIdProvider traceIdProvider) {
        this.logEntryContextManager = logEntryContextManager;
        this.traceIdProvider = traceIdProvider;
    }

    /**
     * 요청이 들어올 때 호출되어, {@link HttpInboundRequestLog}를 생성하고 로깅을 수행한다.
     * <p>
     * - 요청 메소드, URI, 쿼리 문자열, 요청 헤더, 요청 본문, 클라이언트 IP, User-Agent 등을 로그에 기록.
     * - {@link TraceIdProvider}를 통해 Trace ID를 조회하고, 해당 Trace ID를 {@link LogEntryContextManager}를 통해 기록.
     * </p>
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 처리할 핸들러 객체
     * @return {@code true}를 반환하여 필터 체인을 계속 진행시킨다.
     * @throws IOException IO 예외 발생 시
     */

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String traceId = traceIdProvider.getTraceId();
        requestStartTime.set(Instant.now());

        logEntryContextManager.addLog(HttpInboundRequestLog.create(
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

    /**
     * 요청 처리 후 응답을 완료한 뒤 호출되어, {@link HttpInboundResponseLog}를 생성하고 로깅을 수행한다.
     * <p>
     * - 응답 상태 코드, 응답 헤더, 응답 본문, 요청 처리 시간 등을 기록.
     * - 요청의 시작 시간을 기준으로 응답 시간이 기록되며, 이 시간을 통해 요청 처리 성능을 추적 가능하게 한다.
     * </p>
     *
     * @param request HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param handler 처리할 핸들러 객체
     * @param ex 요청 처리 중 발생한 예외 객체
     * @throws IOException IO 예외 발생 시
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws IOException {
        String traceId = traceIdProvider.getTraceId();
        Instant startTime = requestStartTime.get();
        long executionTime = startTime != null ? Instant.now().toEpochMilli() - startTime.toEpochMilli() : 0;

        logEntryContextManager.addLog(HttpInboundResponseLog.create(
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

    /**
     * 요청 헤더를 추출하여 문자열로 반환한다.
     *
     * @param request HTTP 요청 객체
     * @return 요청 헤더를 문자열로 연결한 값
     */
    private String extractHeaders(HttpServletRequest request) {
        StringBuilder headers = new StringBuilder();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.append(headerName).append(": ").append(request.getHeader(headerName)).append("; ");
        }
        return headers.toString();
    }

    /**
     * 응답 헤더를 추출하여 문자열로 반환한다.
     *
     * @param response HTTP 응답 객체
     * @return 응답 헤더를 문자열로 연결한 값
     */
    private String extractHeaders(HttpServletResponse response) {
        StringBuilder headers = new StringBuilder();
        for (String headerName : response.getHeaderNames()) {
            headers.append(headerName).append(": ").append(response.getHeader(headerName)).append("; ");
        }
        return headers.toString();
    }

    /**
     * 요청 본문을 추출하여 문자열로 반환한다.
     *
     * @param request HTTP 요청 객체
     * @return 요청 본문 문자열
     */
    private String extractRequestBody(HttpServletRequest request) {
        if (request instanceof RequestWrapper) {
            byte[] requestBody = ((RequestWrapper) request).getContentAsByteArray();
            return new String(requestBody);
        }
        return "RequestBody Can't read";
    }

    /**
     * 응답 본문을 추출하여 문자열로 반환한다.
     *
     * @param response HTTP 응답 객체
     * @return 응답 본문 문자열
     * @throws IOException IO 예외 발생 시
     */
    private String extractResponseBody(HttpServletResponse response) throws IOException {
        if (response instanceof ContentCachingResponseWrapper wrappedResponse) {
            byte[] contentAsByteArray = wrappedResponse.getContentAsByteArray();
            wrappedResponse.copyBodyToResponse();
            return new String(contentAsByteArray);
        }
        return "ResponseBody Can't read";
    }
}