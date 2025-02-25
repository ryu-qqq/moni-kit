package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.LogEntryContextManager;
import com.monikit.starter.filter.LogContextScopeFilter;
import com.monikit.starter.filter.TraceIdFilter;
/**
 * 필터를 자동으로 등록하는 설정 클래스.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Configuration
public class FilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FilterAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;

    public FilterAutoConfiguration(MoniKitLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    /**
     * ✅ monikit.logging.filters.trace-enabled=true일 때 TraceIdFilter 빈 등록
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.trace-enabled", havingValue = "true", matchIfMissing = true)
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.trace-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(TraceIdFilter traceIdFilter) {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(traceIdFilter);
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");

        logger.info("TraceIdFilter active: {}", loggingProperties.isTraceEnabled());
        registrationBean.setEnabled(loggingProperties.isTraceEnabled());
        return registrationBean;
    }

    /**
     * ✅ monikit.logging.filters.log-enabled=true 일때 LogContextScopeFilter 빈 등록
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public LogContextScopeFilter logContextScopeFilter(LogEntryContextManager logEntryContextManager) {
        return new LogContextScopeFilter(logEntryContextManager);
    }

    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<LogContextScopeFilter> logContextScopeFilterRegistration(LogContextScopeFilter logContextScopeFilter) {
        FilterRegistrationBean<LogContextScopeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(logContextScopeFilter);
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");

        logger.info("LogContextScopeFilter active: {}", loggingProperties.isLogEnabled());
        registrationBean.setEnabled(loggingProperties.isLogEnabled());

        return registrationBean;
    }

}
