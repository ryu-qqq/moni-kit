package com.monikit.starter.web.config;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.web.interceptor.HttpLoggingInterceptor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("HttpLoggingInterceptorConfiguration")
class HttpLoggingInterceptorConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(HttpLoggingInterceptorConfiguration.class))
        .withBean(LogEntryContextManager.class, () -> mock(LogEntryContextManager.class))
        .withBean(TraceIdProvider.class, () -> mock(TraceIdProvider.class));

    @Nested
    @DisplayName("HttpLoggingInterceptor Bean 등록 조건")
    class ConditionalBeanRegistration {

        @Test
        @DisplayName("shouldRegisterBeanWhenLogEnabledTrue")
        void shouldRegisterBeanWhenLogEnabledTrue() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("httpLoggingInterceptor"));
                    assertNotNull(context.getBean(HttpLoggingInterceptor.class));
                });
        }

        @Test
        @DisplayName("shouldRegisterBeanWhenLogEnabledMissing")
        void shouldRegisterBeanWhenLogEnabledMissing() {
            contextRunner
                .run(context -> {
                    assertTrue(context.containsBean("httpLoggingInterceptor"));
                });
        }

        @Test
        @DisplayName("shouldNotRegisterBeanWhenLogEnabledFalse")
        void shouldNotRegisterBeanWhenLogEnabledFalse() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("httpLoggingInterceptor"));
                });
        }
    }
}
