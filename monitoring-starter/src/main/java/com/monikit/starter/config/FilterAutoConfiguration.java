package com.monikit.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

import com.monikit.starter.filter.LogContextScopeFilter;
import com.monikit.starter.filter.TraceIdFilter;

/**
 * 필터를 자동으로 등록하는 설정 클래스.
 * <p>
 * - 사용자가 직접 필터를 등록하지 않아도, `monitoring-starter`를 추가하면 자동으로 적용됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */

@AutoConfiguration
public class FilterAutoConfiguration {

    /**
     * TraceIdFilter를 자동으로 등록하는 빈.
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<TraceIdFilter> traceIdFilter() {
        FilterRegistrationBean<TraceIdFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new TraceIdFilter());
        registrationBean.setOrder(1);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    /**
     * LogContextScopeFilter를 자동으로 등록하는 빈.
     *
     * @return FilterRegistrationBean
     */
    @Bean
    public FilterRegistrationBean<LogContextScopeFilter> logContextScopeFilter() {
        FilterRegistrationBean<LogContextScopeFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new LogContextScopeFilter());
        registrationBean.setOrder(2);
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

}
