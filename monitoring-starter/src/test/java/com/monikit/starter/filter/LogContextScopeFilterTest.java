package com.monikit.starter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.monikit.core.LogEntryContextManager;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("LogContextScopeFilter 테스트")
class LogContextScopeFilterTest {

    private LogContextScopeFilter logContextScopeFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        LogEntryContextManager logEntryContextManager = mock(LogEntryContextManager.class);
        logContextScopeFilter = new LogContextScopeFilter(logEntryContextManager);
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);
    }


    @Test
    @DisplayName("should initialize LogContextScope for each request")
    void shouldInitializeLogContextScopeForEachRequest() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/products");

        logContextScopeFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("should wrap request when request uri is not in excluded paths")
    void shouldWrapRequestWhenRequestUriIsNotExcluded() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/api/products");

        // when
        logContextScopeFilter.doFilterInternal(request, response, filterChain);

        // then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        verify(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

        assertInstanceOf(RequestWrapper.class, requestCaptor.getValue());
        assertInstanceOf(HttpServletResponseWrapper.class, responseCaptor.getValue());
    }



    @Test
    @DisplayName("should not wrap request when request uri is in excluded paths")
    void shouldNotWrapRequestWhenRequestUriIsExcluded() throws ServletException, IOException {
        // given
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // when
        logContextScopeFilter.doFilterInternal(request, response, filterChain);

        // then
        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        verify(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

        assertFalse(requestCaptor.getValue() instanceof RequestWrapper, "Request should not be wrapped");
        assertFalse(responseCaptor.getValue() instanceof HttpServletResponseWrapper, "Response should not be wrapped");
    }

}