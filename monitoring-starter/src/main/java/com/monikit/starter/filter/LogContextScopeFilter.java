package com.monikit.starter.filter;

import java.io.IOException;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.monikit.core.LogEntryContextManager;
import com.monikit.starter.LogContextScope;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.monikit.starter.filter.ExcludePathConstant.EXCLUDED_PATHS;

/**
 * HTTP 요청 단위로 LogContextScope를 관리하는 필터.
 * <p>
 * - 요청이 시작될 때 LogContextScope를 생성하여 자동으로 컨텍스트를 초기화함.
 * - 요청이 끝날 때 자동으로 LogContextScope를 종료하여 컨텍스트를 정리함.
 * - try-with-resources 구문을 활용하여 안전하게 관리됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */

public class LogContextScopeFilter extends OncePerRequestFilter {

    private final LogEntryContextManager logEntryContextManager;

    public LogContextScopeFilter(LogEntryContextManager logEntryContextManager) {
        this.logEntryContextManager = logEntryContextManager;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (EXCLUDED_PATHS.contains(requestUri)) {
            filterChain.doFilter(request, response);
            return;
        }

        try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
            RequestWrapper requestWrapper = new RequestWrapper(request);
            ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

            filterChain.doFilter(requestWrapper, wrappedResponse);
            wrappedResponse.copyBodyToResponse();
        }
    }
}
