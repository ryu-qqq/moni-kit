package com.monikit.starter.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.MetricCollector;

import static org.mockito.Mockito.mock;

@TestConfiguration
public class MockBeanConfig {

    @Bean
    @Primary
    public LogEntryContextManager logEntryContextManager() {
        return mock(LogEntryContextManager.class);
    }

    @Bean
    @Primary
    public MetricCollector metricCollector() {
        return mock(MetricCollector.class);
    }
}
