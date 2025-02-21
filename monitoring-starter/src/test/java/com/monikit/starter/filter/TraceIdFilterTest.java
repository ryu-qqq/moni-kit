package com.monikit.starter.filter;

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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.UUID;

import com.monikit.starter.TraceIdProvider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("TraceIdFilter 테스트")
class TraceIdFilterTest  {

    private TraceIdFilter traceIdFilter;

    @BeforeEach
    void setUp() {
        traceIdFilter = new TraceIdFilter();
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Nested
    @DisplayName("Trace ID가 요청에 포함된 경우")
    class WhenTraceIdExists {

        @Test
        @DisplayName("should use existing X-Trace-Id if present in request header")
        void shouldUseExistingTraceIdIfPresent() throws ServletException, IOException {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain filterChain = mock(FilterChain.class);

            when(request.getHeader("X-Trace-Id")).thenReturn("existing-trace-id");

            doAnswer((Answer<Void>) invocation -> {
                assertEquals("existing-trace-id", MDC.get("traceId"));
                return null;
            })
                .when(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            traceIdFilter.doFilterInternal(request, response, filterChain);

            verify(response).setHeader("X-Trace-Id", "existing-trace-id");

            assertNull(MDC.get("traceId"));
        }

    }

    @Nested
    @DisplayName("Trace ID가 요청에 포함되지 않은 경우")
    class WhenTraceIdIsAbsent {

        @Test
        @DisplayName("should generate new X-Trace-Id if missing in request header")
        void shouldGenerateNewTraceIdIfMissing() throws ServletException, IOException {
            HttpServletRequest request = mock(HttpServletRequest.class);
            HttpServletResponse response = mock(HttpServletResponse.class);
            FilterChain filterChain = mock(FilterChain.class);

            when(request.getHeader("X-Trace-Id")).thenReturn(null);

            doAnswer((Answer<Void>) invocation -> {
                String generatedTraceId = MDC.get("traceId");
                assertNotNull(generatedTraceId);
                assertFalse(generatedTraceId.isEmpty());
                return null;
            })
                .when(filterChain).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

            traceIdFilter.doFilterInternal(request, response, filterChain);

            verify(response).setHeader(eq("X-Trace-Id"), anyString());

            assertNull(MDC.get("traceId"));
        }
    }

    @Test
    @DisplayName("should clear MDC after request processing")
    void shouldClearMdcAfterRequestProcessing() throws ServletException, IOException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(request.getHeader("X-Trace-Id")).thenReturn("trace-id-123");

        traceIdFilter.doFilterInternal(request, response, filterChain);

        assertNull(MDC.get("traceId"));
    }
}