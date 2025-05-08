package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.monikit.core.TraceIdProvider;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.starter.DynamicMatcher;
import com.monikit.starter.ExecutionLoggingAspect;

@Configuration
public class MoniKitAspectConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MoniKitAspectConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    @Lazy
    public ExecutionLoggingAspect executionLoggingAspect(
        LogEntryContextManager logEntryContextManager,
        TraceIdProvider traceIdProvider,
        DynamicMatcher dynamicMatcher
    ) {
        logger.info("[MoniKit] ExecutionLoggingAspect Registered");

        return new ExecutionLoggingAspect(logEntryContextManager, traceIdProvider, dynamicMatcher);
    }
}
