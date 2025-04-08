package com.monikit.starter.web.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.web.filter.LogContextScopeFilter;
import com.monikit.starter.web.filter.TraceIdFilter;

/**
 * 필터를 자동으로 등록하는 설정 클래스.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MoniKitLoggingProperties.class)
public class FilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FilterAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;

    public FilterAutoConfiguration(MoniKitLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.trace-enabled", havingValue = "true", matchIfMissing = true)
    public TraceIdFilter traceIdFilter(TraceIdProvider traceIdProvider) {
        logger.info("Initializing TraceIdFilter with traceEnabled={}", loggingProperties.isTraceEnabled());
        return new TraceIdFilter(traceIdProvider);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.trace-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(TraceIdFilter traceIdFilter) {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(traceIdFilter);
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setEnabled(loggingProperties.isTraceEnabled());
        logger.info("TraceIdFilter active: {}", loggingProperties.isTraceEnabled());
        return registrationBean;
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public LogContextScopeFilter logContextScopeFilter(LogEntryContextManager logEntryContextManager) {
        logger.info("Initializing LogContextScopeFilter with logEnabled={}", loggingProperties.isLogEnabled());
        return new LogContextScopeFilter(logEntryContextManager);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<LogContextScopeFilter> logContextScopeFilterRegistration(LogContextScopeFilter logContextScopeFilter) {
        FilterRegistrationBean<LogContextScopeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(logContextScopeFilter);
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setEnabled(loggingProperties.isLogEnabled());
        logger.info("LogContextScopeFilter active: {}", loggingProperties.isLogEnabled());
        return registrationBean;
    }

}