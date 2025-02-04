package com.monikit.starter.filter;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.monikit.core.MetricCollector;

/**
 * HTTP 요청 메트릭을 Prometheus로 전송하는 필터.
 */
@Component
public class HttpMetricsFilter extends OncePerRequestFilter {

    private final MetricCollector metricCollector;

    public HttpMetricsFilter(MetricCollector metricCollector) {
        this.metricCollector = metricCollector;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            metricCollector.recordHttpRequest(request.getMethod(), request.getRequestURI(), response.getStatus(), duration);
        }
    }

}