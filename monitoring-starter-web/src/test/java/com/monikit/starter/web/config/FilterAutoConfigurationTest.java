package com.monikit.starter.web.config;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.web.filter.LogContextScopeFilter;
import com.monikit.starter.web.filter.TraceIdFilter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@DisplayName("FilterAutoConfiguration 테스트")
class FilterAutoConfigurationTest {

    ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(FilterAutoConfiguration.class))
        .withBean(MoniKitLoggingProperties.class, () -> {
            MoniKitLoggingProperties props = new MoniKitLoggingProperties();
            props.setLogEnabled(true);
            return props;
        })
        .withBean(TraceIdProvider.class, () -> mock(TraceIdProvider.class))
        .withBean(LogEntryContextManager.class, () -> mock(LogEntryContextManager.class));

    @Nested
    @DisplayName("TraceIdFilter 관련 빈 등록 테스트")
    class TraceIdFilterTests {

        @Test
        @DisplayName("TraceIdFilter가 등록되어야 한다")
        void shouldRegisterTraceIdFilter() {
            contextRunner.run(context -> {
                assertTrue(context.containsBean("traceIdFilter"));
                assertTrue(context.getBean("traceIdFilter") instanceof TraceIdFilter);
            });
        }

        @Test
        @DisplayName("TraceIdFilterRegistration이 등록되어야 한다")
        void shouldRegisterTraceIdFilterRegistration() {
            contextRunner.run(context -> {
                assertTrue(context.containsBean("traceIdFilterRegistration"));
                Object registration = context.getBean("traceIdFilterRegistration");
                assertNotNull(registration);
                assertTrue(registration instanceof FilterRegistrationBean);
            });
        }
    }

    @Nested
    @DisplayName("LogContextScopeFilter 관련 빈 등록 테스트")
    class LogContextScopeFilterTests {

        @Test
        @DisplayName("LogContextScopeFilter가 등록되어야 한다")
        void shouldRegisterLogContextScopeFilter() {
            contextRunner.run(context -> {
                assertTrue(context.containsBean("logContextScopeFilter"));
                assertTrue(context.getBean("logContextScopeFilter") instanceof LogContextScopeFilter);
            });
        }

        @Test
        @DisplayName("LogContextScopeFilterRegistration이 등록되어야 한다")
        void shouldRegisterLogContextScopeFilterRegistration() {
            contextRunner.run(context -> {
                assertTrue(context.containsBean("logContextScopeFilterRegistration"));
                Object registration = context.getBean("logContextScopeFilterRegistration");
                assertNotNull(registration);
                assertTrue(registration instanceof FilterRegistrationBean);
            });
        }
    }
}
