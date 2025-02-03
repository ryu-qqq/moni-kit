package com.monikit.starter.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;

import java.io.IOException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@DisplayName("LogContextScopeFilter 테스트")
class LogContextScopeFilterTest {

    private final LogContextScopeFilter logContextScopeFilter = new LogContextScopeFilter();

    @Test
    @DisplayName("should initialize LogContextScope for each request")
    void shouldInitializeLogContextScopeForEachRequest() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        logContextScopeFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    @DisplayName("should wrap request and response correctly")
    void shouldWrapRequestAndResponseCorrectly() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        logContextScopeFilter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<HttpServletRequest> requestCaptor = ArgumentCaptor.forClass(HttpServletRequest.class);
        ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        verify(filterChain).doFilter(requestCaptor.capture(), responseCaptor.capture());

        assertInstanceOf(RequestWrapper.class, requestCaptor.getValue());
        assertInstanceOf(HttpServletResponseWrapper.class, responseCaptor.getValue());
    }

}