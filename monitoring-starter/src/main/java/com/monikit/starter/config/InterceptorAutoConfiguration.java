package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.monikit.core.LogEntryContextManager;
import com.monikit.starter.interceptor.HttpLoggingInterceptor;


/**
 * HTTP 인터셉터를 자동으로 등록하는 설정 클래스.
 * <p>
 * - `monikit.logging.filters.log-enabled=true`일 때만 HttpLoggingInterceptor를 등록한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties(MoniKitLoggingProperties.class)
@ConditionalOnProperty(name = "monikit.logging.filters.log-enabled", havingValue = "true", matchIfMissing = true)
public class InterceptorAutoConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(InterceptorAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;

    public InterceptorAutoConfiguration(MoniKitLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    /**
     * ✅ `monikit.logging.filters.log-enabled=true`일 때만 HttpLoggingInterceptor 빈을 생성.
     */
    @Bean
    public HttpLoggingInterceptor httpLoggingInterceptor(LogEntryContextManager logEntryContextManager) {
        return new HttpLoggingInterceptor(logEntryContextManager);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("monikit.logging.filters.log-enabled = {}", loggingProperties.isLogEnabled());

        if (!loggingProperties.isLogEnabled()) {
            logger.info("HTTP Logging Interceptor active off");
            return;
        }

        logger.info("HTTP Logging Interceptor active on");
        registry.addInterceptor(httpLoggingInterceptor(null))
            .addPathPatterns("/**");
    }
}