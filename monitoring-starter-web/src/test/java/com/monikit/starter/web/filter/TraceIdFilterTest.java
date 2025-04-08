package com.monikit.starter.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;
import org.slf4j.MDC;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;

import java.io.IOException;

import com.monikit.core.TraceIdProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("TraceIdFilter 단위 테스트")
class TraceIdFilterTest {

    TraceIdProvider mockTraceIdProvider = mock(TraceIdProvider.class);
    TraceIdFilter filter = new TraceIdFilter(mockTraceIdProvider);

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    FilterChain filterChain = mock(FilterChain.class);

    @Nested
    @DisplayName("X-Trace-Id 헤더가 없는 경우")
    class WhenHeaderMissing {

        @Test
        @DisplayName("shouldGenerateNewTraceIdAndSetHeader")
        void shouldGenerateNewTraceIdAndSetHeader() throws ServletException, IOException {
            when(request.getHeader("X-Trace-Id")).thenReturn(null);

            filter.doFilterInternal(request, response, filterChain);

            verify(mockTraceIdProvider).setTraceId(anyString());
            verify(response).setHeader(eq("X-Trace-Id"), anyString());
            verify(filterChain).doFilter(request, response);
            verify(mockTraceIdProvider).clear();
        }
    }

    @Nested
    @DisplayName("X-Trace-Id 헤더가 존재하는 경우")
    class WhenHeaderExists {

        @Test
        @DisplayName("shouldUseProvidedTraceIdAndSetSameToResponse")
        void shouldUseProvidedTraceIdAndSetSameToResponse() throws ServletException, IOException {
            String existingTraceId = "trace-1234";
            when(request.getHeader("X-Trace-Id")).thenReturn(existingTraceId);

            filter.doFilterInternal(request, response, filterChain);

            verify(mockTraceIdProvider).setTraceId(existingTraceId);
            verify(response).setHeader("X-Trace-Id", existingTraceId);
            verify(filterChain).doFilter(request, response);
            verify(mockTraceIdProvider).clear();
        }
    }

    @Test
    @DisplayName("shouldClearTraceIdOnException")
    void shouldClearTraceIdOnException() throws ServletException, IOException {
        when(request.getHeader("X-Trace-Id")).thenReturn("any-trace");
        doThrow(new RuntimeException("test ex")).when(filterChain).doFilter(request, response);

        assertThrows(RuntimeException.class, () -> {
            filter.doFilterInternal(request, response, filterChain);
        });

        verify(mockTraceIdProvider).clear();
    }
}