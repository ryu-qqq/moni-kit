package com.monikit.starter.web.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.web.filter.LogContextScopeFilter;
import com.monikit.starter.web.filter.TraceIdFilter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("FilterAutoConfiguration")
class FilterAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FilterAutoConfiguration.class))
        .withBean(LogEntryContextManager.class, () -> mock(LogEntryContextManager.class))
        .withBean(TraceIdProvider.class, () -> mock(TraceIdProvider.class));

    @Nested
    @DisplayName("trace-enabled 설정")
    class TraceIdFilterRegistration {

        @Test
        @DisplayName("shouldRegisterTraceIdFilterWhenTraceEnabledTrue")
        void shouldRegisterTraceIdFilterWhenTraceEnabledTrue() {
            contextRunner
                .withPropertyValues("monikit.logging.trace-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("traceIdFilter"));
                    assertTrue(context.containsBean("traceIdFilterRegistration"));

                    TraceIdFilter filter = context.getBean(TraceIdFilter.class);
                    FilterRegistrationBean<?> registrationBean = context.getBean("traceIdFilterRegistration", FilterRegistrationBean.class);
                    assertEquals(filter, registrationBean.getFilter());
                    assertTrue(registrationBean.isEnabled());
                });
        }

        @Test
        @DisplayName("shouldNotRegisterTraceIdFilterWhenTraceEnabledFalse")
        void shouldNotRegisterTraceIdFilterWhenTraceEnabledFalse() {
            contextRunner
                .withPropertyValues("monikit.logging.trace-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("traceIdFilter"));
                    assertFalse(context.containsBean("traceIdFilterRegistration"));
                });
        }
    }

    @Nested
    @DisplayName("log-enabled 설정")
    class LogContextScopeFilterRegistration {

        @Test
        @DisplayName("shouldRegisterLogContextScopeFilterWhenLogEnabledTrue")
        void shouldRegisterLogContextScopeFilterWhenLogEnabledTrue() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("logContextScopeFilter"));
                    assertTrue(context.containsBean("logContextScopeFilterRegistration"));

                    LogContextScopeFilter filter = context.getBean(LogContextScopeFilter.class);
                    FilterRegistrationBean<?> registrationBean = context.getBean("logContextScopeFilterRegistration", FilterRegistrationBean.class);
                    assertEquals(filter, registrationBean.getFilter());
                    assertTrue(registrationBean.isEnabled());
                });
        }

        @Test
        @DisplayName("shouldNotRegisterLogContextScopeFilterWhenLogEnabledFalse")
        void shouldNotRegisterLogContextScopeFilterWhenLogEnabledFalse() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("logContextScopeFilter"));
                    assertFalse(context.containsBean("logContextScopeFilterRegistration"));
                });
        }
    }
}
