package com.monikit.starter.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.TraceIdProvider;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.starter.web.MoniKitWebProperties;
import com.monikit.starter.web.filter.LogContextScopeFilter;
import com.monikit.starter.web.filter.TraceIdFilter;

/**
 * 필터를 자동으로 등록하는 설정 클래스.
 *
 * @author ryu-qqq
 * @since 1.0.3
 */
@Configuration
public class FilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FilterAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;
    private final MoniKitWebProperties webProperties;

    public FilterAutoConfiguration(MoniKitLoggingProperties loggingProperties, MoniKitWebProperties webProperties) {
        this.loggingProperties = loggingProperties;
        this.webProperties = webProperties;
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.log-enabled", havingValue = "true", matchIfMissing = true)
    public TraceIdFilter traceIdFilter(TraceIdProvider traceIdProvider) {
        logger.info("[MoniKit] Initializing TraceIdFilter with traceEnabled={}", loggingProperties.isLogEnabled());
        return new TraceIdFilter(traceIdProvider);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.log-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(TraceIdFilter traceIdFilter) {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(traceIdFilter);
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setEnabled(loggingProperties.isLogEnabled());
        logger.info("[MoniKit] TraceIdFilter active: {}", loggingProperties.isLogEnabled());
        return registrationBean;
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.log-enabled", havingValue = "true", matchIfMissing = true)
    public LogContextScopeFilter logContextScopeFilter(LogEntryContextManager logEntryContextManager) {
        logger.info("[MoniKit] Initializing LogContextScopeFilter with logEnabled={}", loggingProperties.isLogEnabled());
        return new LogContextScopeFilter(logEntryContextManager, webProperties);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.log-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<LogContextScopeFilter> logContextScopeFilterRegistration(LogContextScopeFilter logContextScopeFilter) {
        FilterRegistrationBean<LogContextScopeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(logContextScopeFilter);
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");
        registrationBean.setEnabled(loggingProperties.isLogEnabled());
        logger.info("[MoniKit] LogContextScopeFilter active: {}", loggingProperties.isLogEnabled());
        return registrationBean;
    }

}