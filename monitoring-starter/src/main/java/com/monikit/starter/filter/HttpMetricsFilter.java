package com.monikit.starter.filter;

import java.io.IOException;
import java.util.Set;

import org.springframework.web.filter.OncePerRequestFilter;

import com.monikit.core.MetricCollector;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static com.monikit.starter.filter.ExcludePathConstant.EXCLUDED_PATHS;

/**
 * HTTP 요청 메트릭을 Prometheus로 전송하는 필터.
 */

public class HttpMetricsFilter extends OncePerRequestFilter {

    private final MetricCollector metricCollector;


    public HttpMetricsFilter(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        String requestUri = request.getRequestURI();

        try {
            filterChain.doFilter(request, response);
        } finally {
            if (!EXCLUDED_PATHS.contains(requestUri)) {
                long duration = System.currentTimeMillis() - startTime;
                int status = response.getStatus() > 0 ? response.getStatus() : 500;
                metricCollector.recordHttpRequest(request.getMethod(), requestUri, status, duration);
            }
        }
    }

}
