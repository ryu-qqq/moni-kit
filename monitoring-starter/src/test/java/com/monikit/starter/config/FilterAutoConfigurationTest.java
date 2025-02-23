package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.starter.filter.HttpMetricsFilter;
import com.monikit.starter.filter.LogContextScopeFilter;
import com.monikit.starter.filter.TraceIdFilter;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link FilterAutoConfiguration}의 동작을 검증하는 테스트 클래스.
 */
class FilterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(FilterAutoConfiguration.class))
            .withUserConfiguration(MockBeanConfig.class);

    @Nested
    @DisplayName("TraceIdFilter 자동 등록 테스트")
    class TraceIdFilterAutoConfigurationTests {

        @Test
        @DisplayName("monikit.logging.filters.trace-enabled=true 설정 시 TraceIdFilter가 등록되어야 한다")
        void shouldRegisterTraceIdFilterWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.trace-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("traceIdFilter"));
                    assertTrue(context.containsBean("traceIdFilterRegistration"));
                    assertNotNull(context.getBean(TraceIdFilter.class));
                });
        }

        @Test
        @DisplayName("monikit.logging.filters.trace-enabled=false 설정 시 TraceIdFilter가 등록되지 않아야 한다")
        void shouldNotRegisterTraceIdFilterWhenDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.trace-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("traceIdFilter"));
                    assertFalse(context.containsBean("traceIdFilterRegistration"));
                });
        }
    }

    @Nested
    @DisplayName("HttpMetricsFilter 자동 등록 테스트")
    class HttpMetricsFilterAutoConfigurationTests {

        @Test
        @DisplayName("monikit.logging.filters.metrics-enabled=true 설정 시 HttpMetricsFilter가 등록되어야 한다")
        void shouldRegisterHttpMetricsFilterWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.metrics-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("httpMetricsFilter"));
                    assertTrue(context.containsBean("httpMetricsFilterRegistration"));
                    assertNotNull(context.getBean(HttpMetricsFilter.class));
                });
        }

        @Test
        @DisplayName("monikit.logging.filters.metrics-enabled=false 설정 시 HttpMetricsFilter가 등록되지 않아야 한다")
        void shouldNotRegisterHttpMetricsFilterWhenDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.metrics-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("httpMetricsFilter"));
                    assertFalse(context.containsBean("httpMetricsFilterRegistration"));
                });
        }
    }

    @Nested
    @DisplayName("LogContextScopeFilter 자동 등록 테스트")
    class LogContextScopeFilterAutoConfigurationTests {
        @Test
        @DisplayName("monikit.logging.filters.log-enabled=true 설정 시 LogContextScopeFilter는 등록되어야 한다")
        void shouldRegisterLogContextScopeFilterWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.log-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("logContextScopeFilter"));
                    assertTrue(context.containsBean("logContextScopeFilterRegistration"));
                    assertNotNull(context.getBean(LogContextScopeFilter.class));
                });
        }

        @Test
        @DisplayName("monikit.logging.filters.log-enabled=false 설정 시 LogContextScopeFilter가 등록되지 않아야 한다")
        void shouldNotRegisterLogContextScopeFilterWhenDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.log-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("logContextScopeFilter"));
                    assertFalse(context.containsBean("logContextScopeFilterRegistration"));
                });
        }

    }
}

