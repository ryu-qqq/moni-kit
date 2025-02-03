package com.monikit.starter.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.monikit.starter.interceptor.HttpLoggingInterceptor;

/**
 * HTTP 인터셉터를 자동으로 등록하는 설정 클래스.
 * <p>
 * - 사용자가 별도로 인터셉터를 등록하지 않아도 자동으로 적용됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@AutoConfiguration
public class InterceptorAutoConfiguration implements WebMvcConfigurer {

    @Bean
    public HttpLoggingInterceptor httpLoggingInterceptor() {
        return new HttpLoggingInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(httpLoggingInterceptor())
            .addPathPatterns("/**"); // 모든 요청을 감지하여 로깅 적용
    }

}