package com.monikit.starter.config;

import java.util.List;

import com.monikit.core.TraceIdProvider;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.model.LogEntry;
import com.monikit.starter.DynamicMatcher;
import com.monikit.starter.ExecutionLoggingAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.jupiter.api.Assertions.*;

class MoniKitAspectConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(MoniKitAspectConfiguration.class, MockCoreBeans.class));

    @Test
    @DisplayName("shouldRegisterExecutionLoggingAspectIfAllDependenciesProvided")
    void shouldRegisterExecutionLoggingAspectIfAllDependenciesProvided() {
        contextRunner.run(context -> {
            assertTrue(context.containsBean("executionLoggingAspect"));
            assertNotNull(context.getBean(ExecutionLoggingAspect.class));
        });
    }

    @Test
    @DisplayName("shouldNotRegisterExecutionLoggingAspectWhenCustomBeanProvided")
    void shouldNotRegisterExecutionLoggingAspectWhenCustomBeanProvided() {
        contextRunner
            .withUserConfiguration(CustomExecutionLoggingAspectConfig.class)
            .run(context -> {
                ExecutionLoggingAspect aspect = context.getBean(ExecutionLoggingAspect.class);
                assertEquals("custom", aspect.toString());
            });
    }

    @TestConfiguration
    static class MockCoreBeans {

        @Bean
        public LogEntryContextManager logEntryContextManager() {
            return new LogEntryContextManager() {
                @Override public void addLog(LogEntry logEntry) {}
                @Override public void flush() {}
                @Override public void clear() {}
            };
        }

        @Bean
        public TraceIdProvider traceIdProvider() {
            return new TraceIdProvider() {
                private String id;
                @Override public String getTraceId() { return id; }
                @Override public void setTraceId(String traceId) { this.id = traceId; }
                @Override public void clear() { this.id = null; }
            };
        }

        @Bean
        public DynamicMatcher dynamicMatcher() {
            return new DynamicMatcher(List.of(), java.util.List.of("com.monikit"));
        }
    }

    @Configuration
    static class CustomExecutionLoggingAspectConfig {

        @Bean
        public ExecutionLoggingAspect executionLoggingAspect(LogEntryContextManager manager,
                                                             TraceIdProvider traceIdProvider,
                                                             DynamicMatcher matcher) {
            return new ExecutionLoggingAspect(manager, traceIdProvider, matcher) {
                @Override public String toString() {
                    return "custom";
                }
            };
        }
    }
}
