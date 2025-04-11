package com.monikit.starter.web.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.starter.web.interceptor.HttpLoggingInterceptor;
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
public class InterceptorAutoConfiguration implements WebMvcConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(InterceptorAutoConfiguration.class);

    private final MoniKitLoggingProperties loggingProperties;

    @Autowired(required = false)
    private HttpLoggingInterceptor httpLoggingInterceptor;

    public InterceptorAutoConfiguration(MoniKitLoggingProperties loggingProperties) {
        this.loggingProperties = loggingProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        logger.info("monikit.logging.log-enabled = {}", loggingProperties.isLogEnabled());

        if (!loggingProperties.isLogEnabled() || httpLoggingInterceptor == null) {
            logger.info("[MoniKit] HTTP Logging Interceptor active off");
            return;
        }

        logger.info("[MoniKit] HTTP Logging Interceptor active on");
        registry.addInterceptor(httpLoggingInterceptor).addPathPatterns("/**");
    }
}