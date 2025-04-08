package com.monikit.starter.webflux.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.webflux.filter.LogContextScopeGatewayFilter;
import com.monikit.starter.webflux.filter.TraceIdGatewayFilter;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class GatewayFilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(GatewayFilterAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;

    public GatewayFilterAutoConfiguration(MoniKitLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    /**
     * TraceIdGatewayFilter 등록
     * `monikit.logging.filters.trace-enabled`가 true일 때만 활성화.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.trace-enabled", havingValue = "true", matchIfMissing = true)
    public TraceIdGatewayFilter traceIdGatewayFilter(TraceIdProvider traceIdProvider) {
        return new TraceIdGatewayFilter(traceIdProvider);
    }

    /**
     * TraceIdGatewayFilter 등록
     * `monikit.logging.filters.trace-enabled`가 true일 때만 활성화.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.trace-enabled", havingValue = "true", matchIfMissing = true)
    public List<GatewayFilter> traceIdGatewayFilterRegistration(TraceIdGatewayFilter traceIdGatewayFilter) {
        logger.info("TraceIdGatewayFilter active: {}", loggingProperties.isTraceEnabled());
        return List.of(traceIdGatewayFilter);
    }

    /**
     * LogContextScopeGatewayFilter 등록
     * `monikit.logging.filters.log-enabled`가 true일 때만 활성화.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public LogContextScopeGatewayFilter logContextScopeGatewayFilter(LogEntryContextManager logEntryContextManager, TraceIdProvider traceIdProvider) {
        return new LogContextScopeGatewayFilter(logEntryContextManager, traceIdProvider);
    }

    /**
     * LogContextScopeGatewayFilter 등록
     * `monikit.logging.filters.log-enabled`가 true일 때만 활성화.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public List<GatewayFilter> logContextScopeGatewayFilterRegistration(LogContextScopeGatewayFilter logContextScopeGatewayFilter) {
        logger.info("LogContextScopeGatewayFilter active: {}", loggingProperties.isLogEnabled());
        return List.of(logContextScopeGatewayFilter);
    }
}