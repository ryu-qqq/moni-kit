package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.MetricCollector;
import com.monikit.starter.filter.HttpMetricsFilter;
import com.monikit.starter.filter.LogContextScopeFilter;
import com.monikit.starter.filter.TraceIdFilter;
/**
 * 필터를 자동으로 등록하는 설정 클래스.
 * <p>
 * - 사용자가 직접 필터를 등록하지 않아도, `monitoring-starter`를 추가하면 자동으로 적용됨.
 * - 특정 필터를 활성화할지 여부는 `monikit.logging.filters.*` 설정에 따라 결정됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class FilterAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(FilterAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;
    private final MoniKitMetricsProperties metricsProperties;

    public FilterAutoConfiguration(MoniKitLoggingProperties loggingProperties,
                                   MoniKitMetricsProperties metricsProperties) {
        this.loggingProperties = loggingProperties;
        this.metricsProperties = metricsProperties;
    }

    /**
     * TraceIdFilter 빈을 등록 (사용자가 직접 구현한 필터가 없을 경우 기본 제공).
     */
    @Bean
    @ConditionalOnMissingBean(TraceIdFilter.class)
    public TraceIdFilter traceIdFilter() {
        return new TraceIdFilter();
    }

    /**
     * TraceIdFilter를 자동으로 등록하는 빈.
     * - `monikit.logging.filters.trace-enabled=true`일 때만 활성화.
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilterRegistration(TraceIdFilter traceIdFilter) {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(traceIdFilter);
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");

        if (!loggingProperties.isTraceEnabled()) {
            registrationBean.setEnabled(false);
            logger.info("TraceIdFilter active off (monikit.logging.filters.trace-enabled=false)");
        } else {
            logger.info("TraceIdFilter active on");
        }
        return registrationBean;
    }

    /**
     * LogContextScopeFilter는 무조건 활성화 (필수 필터)
     */
    @Bean
    public FilterRegistrationBean<LogContextScopeFilter> logContextScopeFilter(
        LogEntryContextManager logEntryContextManager) {
        FilterRegistrationBean<LogContextScopeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LogContextScopeFilter(logEntryContextManager));
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");
        logger.info("LogContextScopeFilter active on");
        return registrationBean;
    }

    /**
     * HttpMetricsFilter 빈을 등록 (사용자가 직접 구현한 필터가 없을 경우 기본 제공).
     */
    @Bean
    @ConditionalOnMissingBean(HttpMetricsFilter.class)
    public HttpMetricsFilter httpMetricsFilter(MetricCollector metricCollector) {
        return new HttpMetricsFilter(metricCollector);
    }

    /**
     * HttpMetricsFilter를 자동으로 등록하는 빈.
     * - `monikit.logging.filters.metrics-enabled=true`일 때만 활성화.
     */
    @Bean
    public FilterRegistrationBean<HttpMetricsFilter> httpMetricsFilterRegistration(HttpMetricsFilter httpMetricsFilter) {
        FilterRegistrationBean<HttpMetricsFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(httpMetricsFilter);
        registrationBean.setOrder(3);
        registrationBean.addUrlPatterns("/*");

        if (!metricsProperties.isMetricsEnabled()) {
            registrationBean.setEnabled(false);
            logger.info("HttpMetricsFilter active off (monikit.logging.filters.metrics-enabled=false)");
        } else {
            logger.info("HttpMetricsFilter active on");
        }
        return registrationBean;
    }
}