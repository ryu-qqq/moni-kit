package com.monikit.starter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.monikit.core.MetricCollector;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HttpMetricsFilterTest {

    private MetricCollector metricCollector;
    private HttpMetricsFilter httpMetricsFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        metricCollector = mock(MetricCollector.class);
        httpMetricsFilter = new HttpMetricsFilter(metricCollector);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }

    @Test
    @DisplayName("should not record metrics when request URI is in excluded paths")
    void shouldNotRecordMetricsForExcludedPaths() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // when
        httpMetricsFilter.doFilterInternal(request, response, filterChain);

        // then
        verify(filterChain, times(1)).doFilter(request, response);
        verify(metricCollector, never()).recordHttpRequest(any(), any(), anyInt(), anyLong());
    }

    @Test
    @DisplayName("should record metrics when request URI is not in excluded paths")
    void shouldRecordMetricsForNonExcludedPaths() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getMethod()).thenReturn("GET");
        when(response.getStatus()).thenReturn(200);

        // when
        httpMetricsFilter.doFilterInternal(request, response, filterChain);

        // then
        ArgumentCaptor<String> methodCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(metricCollector, times(1)).recordHttpRequest(
            methodCaptor.capture(), uriCaptor.capture(), statusCaptor.capture(), durationCaptor.capture()
        );

        assertEquals("GET", methodCaptor.getValue());
        assertEquals("/api/products", uriCaptor.getValue());
        assertEquals(200, statusCaptor.getValue());
        assertTrue(durationCaptor.getValue() >= 0, "Execution time should be non-negative");
    }

    @Test
    @DisplayName("should record metrics even when an exception occurs")
    void shouldRecordMetricsEvenWhenExceptionOccurs() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/api/products");
        when(request.getMethod()).thenReturn("POST");
        when(response.getStatus()).thenReturn(500);

        doThrow(new ServletException("Test Exception")).when(filterChain).doFilter(request, response);

        // when
        assertThrows(ServletException.class, () -> httpMetricsFilter.doFilterInternal(request, response, filterChain));

        // then
        ArgumentCaptor<String> methodCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> uriCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Integer> statusCaptor = ArgumentCaptor.forClass(Integer.class);
        ArgumentCaptor<Long> durationCaptor = ArgumentCaptor.forClass(Long.class);

        verify(filterChain, times(1)).doFilter(request, response);
        verify(metricCollector, times(1)).recordHttpRequest(
            methodCaptor.capture(), uriCaptor.capture(), statusCaptor.capture(), durationCaptor.capture()
        );

        assertEquals("POST", methodCaptor.getValue());
        assertEquals("/api/products", uriCaptor.getValue());
        assertEquals(500, statusCaptor.getValue());
        assertTrue(durationCaptor.getValue() >= 0, "Execution time should be non-negative");
    }


}