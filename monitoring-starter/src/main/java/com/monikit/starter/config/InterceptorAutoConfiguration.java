package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.monikit.core.LogEntryContextManager;
import com.monikit.starter.interceptor.HttpLoggingInterceptor;

/**
 * HTTP 인터셉터를 자동으로 등록하는 설정 클래스.
 * <p>
 * - 사용자가 별도로 인터셉터를 등록하지 않아도 자동으로 적용됨.
 * - `monikit.logging.interceptors-enabled=false`로 설정하면 비활성화 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class InterceptorAutoConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(InterceptorAutoConfiguration.class);
    private final MoniKitLoggingProperties loggingProperties;

    public InterceptorAutoConfiguration(MoniKitLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    /**
     * HttpLoggingInterceptor 빈을 등록 (사용자가 직접 구현한 경우 해당 빈 사용).
     */
    @Bean
    @ConditionalOnMissingBean(HttpLoggingInterceptor.class)
    public HttpLoggingInterceptor httpLoggingInterceptor(LogEntryContextManager logEntryContextManager) {
        return new HttpLoggingInterceptor(logEntryContextManager);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (!loggingProperties.isInterceptorsEnabled()) {
            logger.info("HTTP interceptors active off (monikit.logging.interceptors-enabled=false)");
            return;
        }

        logger.info("HTTP interceptors active on");
        registry.addInterceptor(httpLoggingInterceptor(null))
            .addPathPatterns("/**");
    }
}
