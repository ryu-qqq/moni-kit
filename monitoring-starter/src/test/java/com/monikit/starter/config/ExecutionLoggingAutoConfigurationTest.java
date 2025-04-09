package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.ExecutionLoggingAspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionLoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(
            ExecutionLoggingAutoConfiguration.class,
            MockCoreBeans.class
        ));

    @Test
    @DisplayName("shouldRegisterExecutionLoggingAspectWhenDetailedLoggingIsEnabled")
    void shouldRegisterExecutionLoggingAspectWhenDetailedLoggingIsEnabled() {
        contextRunner
            .withPropertyValues("monikit.logging.detailed-logging=true")
            .run(context -> {
                assertTrue(context.containsBean("executionLoggingAspect"));
                assertNotNull(context.getBean(ExecutionLoggingAspect.class));
            });
    }

    @Test
    @DisplayName("shouldNotRegisterExecutionLoggingAspectWhenDetailedLoggingIsDisabled")
    void shouldNotRegisterExecutionLoggingAspectWhenDetailedLoggingIsDisabled() {
        contextRunner
            .withPropertyValues("monikit.logging.detailed-logging=false")
            .run(context -> assertFalse(context.containsBean("executionLoggingAspect")));
    }

    @Test
    @DisplayName("shouldNotRegisterExecutionLoggingAspectWhenPropertyIsMissing")
    void shouldNotRegisterExecutionLoggingAspectWhenPropertyIsMissing() {
        contextRunner.run(context -> assertFalse(context.containsBean("executionLoggingAspect")));
    }

    @Test
    @DisplayName("shouldNotRegisterExecutionLoggingAspectWhenCustomBeanIsProvided")
    void shouldNotRegisterExecutionLoggingAspectWhenCustomBeanIsProvided() {
        contextRunner
            .withPropertyValues("monikit.logging.detailed-logging=true")
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
            return new LogEntryContextManager(){

                @Override
                public void addLog(LogEntry logEntry) {

                }

                @Override
                public void flush() {

                }

                @Override
                public void clear() {

                }
            };
        }

        @Bean
        public MoniKitLoggingProperties loggingProperties() {
            return new MoniKitLoggingProperties();
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
    }

    @Configuration
    static class CustomExecutionLoggingAspectConfig {

        @Bean
        public ExecutionLoggingAspect executionLoggingAspect(LogEntryContextManager manager,
                                                             MoniKitLoggingProperties properties,
                                                             TraceIdProvider traceIdProvider) {
            return new ExecutionLoggingAspect(manager, properties, traceIdProvider) {
                @Override public String toString() {
                    return "custom";
                }
            };
        }
    }



}