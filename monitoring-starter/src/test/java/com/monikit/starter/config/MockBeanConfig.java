package com.monikit.starter.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.monikit.core.LogEntryContextManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
public class MockBeanConfig {

    @Bean
    @Primary
    public LogEntryContextManager logEntryContextManager() {
        return mock(LogEntryContextManager.class);
    }

    @Bean
    @Primary
    public MoniKitLoggingProperties mockMoniKitLoggingProperties() {
        MoniKitLoggingProperties properties = mock(MoniKitLoggingProperties.class);
        when(properties.isTraceEnabled()).thenReturn(true);
        when(properties.isLogEnabled()).thenReturn(true);
        return properties;
    }

}