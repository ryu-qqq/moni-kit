package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.monikit.starter.interceptor.HttpLoggingInterceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InterceptorAutoConfigurationTest {


    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(InterceptorAutoConfiguration.class))
            .withUserConfiguration(MockBeanConfig.class);


    @Nested
    @DisplayName("HttpLoggingInterceptor 자동 등록 테스트")
    class HttpLoggingInterceptorAutoConfigurationTests {

        @Test
        @DisplayName("monikit.logging.filters.log-enabled=true 설정 시 HttpLoggingInterceptor가 등록되어야 한다")
        void shouldRegisterHttpLoggingInterceptorWhenEnabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.log-enabled=true")
                .run(context -> {
                    assertTrue(context.containsBean("httpLoggingInterceptor"));
                    assertTrue(context.containsBean("interceptorAutoConfiguration"));
                    assertNotNull(context.getBean(HttpLoggingInterceptor.class));
                    assertNotNull(context.getBean(WebMvcConfigurer.class));

                });
        }

        @Test
        @DisplayName("monikit.logging.filters.log-enabled=false 설정 시 HttpLoggingInterceptor가 등록되지 않아야 한다")
        void shouldNotRegisterHttpLoggingInterceptorWhenDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.log-enabled=false")
                .run(context -> {
                    assertFalse(context.containsBean("httpLoggingInterceptor"));
                    assertFalse(context.containsBean("interceptorAutoConfiguration"));
                });
        }
    }

}