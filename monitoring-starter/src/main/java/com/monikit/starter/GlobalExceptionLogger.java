package com.monikit.starter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;

/**
 * 전역 예외를 감지하고 로깅하는 Resolver.
 * <p>
 * 사용자의 @RestControllerAdvice와 관계없이 모든 예외를 로깅 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Component
public class GlobalExceptionLogger implements HandlerExceptionResolver {

    @Override
    public ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response, Object handler, Exception e) {
        String traceId = TraceIdProvider.currentTraceId();
        LogEntryContextManager.logException(traceId, e, ErrorCategoryClassifier.categorize(e));
        return null;
    }

}
