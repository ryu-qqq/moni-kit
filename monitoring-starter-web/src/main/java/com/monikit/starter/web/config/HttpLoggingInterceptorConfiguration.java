package com.monikit.starter.web.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.web.interceptor.HttpLoggingInterceptor;

/**
 * HTTP 로깅 인터셉터 빈을 생성하는 설정 클래스
 * - monikit.logging.filters.log-enabled=true 일 때만 인터셉터를 등록
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
@Configuration
@EnableConfigurationProperties(MoniKitLoggingProperties.class)
public class HttpLoggingInterceptorConfiguration {

    /**
     * `monikit.logging.filters.log-enabled=true`일 때만 HttpLoggingInterceptor 빈을 생성.
     */
    @Bean
    @ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
    public HttpLoggingInterceptor httpLoggingInterceptor(LogEntryContextManager logEntryContextManager, TraceIdProvider traceIdProvider) {
        return new HttpLoggingInterceptor(logEntryContextManager, traceIdProvider);
    }
}
