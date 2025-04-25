package com.monikit.starter.web.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.monikit.core.LogContextScope;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.starter.web.MoniKitWebProperties;

import static com.monikit.starter.web.ExcludePathConstant.EXCLUDED_PATHS;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 요청 단위로 {@link LogContextScope}를 관리하는 필터.
 * <p>
 * - 요청이 시작될 때 {@link LogContextScope}를 생성하여 자동으로 컨텍스트를 초기화함.
 * - 요청이 끝날 때 자동으로 {@link LogContextScope}를 종료하여 컨텍스트를 정리함.
 * - 요청 및 응답을 감싸는 {@link RequestWrapper}와 {@link ContentCachingResponseWrapper}를 사용하여
 *   요청과 응답 데이터를 안전하게 처리함.
 * - try-with-resources 구문을 활용하여 {@link LogContextScope}를 안전하게 관리하며, 필터 체인의 후속 처리를 위한
 *   {@link FilterChain#doFilter} 호출을 수행함.
 * </p>
 *
 * <p>
 * 사용 예시:
 * <pre>{@code
 * // 필터가 실행되면 요청을 래핑하고, 응답 본문도 캡처하여 로그를 남길 수 있음
 * }</pre>
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 * @see LogContextScope
 * @see com.monikit.starter.web.filter.RequestWrapper
 * @see org.springframework.web.util.ContentCachingResponseWrapper
 */

public class LogContextScopeFilter extends OncePerRequestFilter {

    private final LogEntryContextManager logEntryContextManager;
    private final List<String> excludedPaths;

    /**
     * 새로운 {@link LogContextScopeFilter} 인스턴스를 생성한다.
     *
     * @param logEntryContextManager {@link LogEntryContextManager} 인스턴스
     * @param webProperties {@link MoniKitWebProperties} 인스턴스
     */
    public LogContextScopeFilter(LogEntryContextManager logEntryContextManager, MoniKitWebProperties webProperties) {
        this.logEntryContextManager = logEntryContextManager;
        this.excludedPaths = webProperties.getExcludedPaths();
    }

    /**
     * HTTP 요청과 응답을 필터링하고, {@link LogContextScope}를 생성 및 종료한다.
     * <p>
     * - 요청 URI가 제외 경로에 포함되지 않으면 {@link LogContextScope}를 생성하여 로깅 컨텍스트를 초기화한다.
     * - 요청 및 응답 데이터를 {@link RequestWrapper}와 {@link ContentCachingResponseWrapper}로 감싸
     *   안전하게 처리하고, 필터 체인을 계속 진행한다.
     * - 응답이 완료되면, 캡처된 응답 본문을 원래 응답에 복사한다.
     * </p>
     *
     * @param request  HTTP 요청 객체
     * @param response HTTP 응답 객체
     * @param filterChain 필터 체인
     * @throws ServletException 서블릿 예외 발생 시
     * @throws IOException IO 예외 발생 시
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (excludedPaths.contains(requestUri)) {
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
