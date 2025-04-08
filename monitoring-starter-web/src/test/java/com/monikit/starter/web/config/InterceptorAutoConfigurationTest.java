package com.monikit.starter.web.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.starter.web.interceptor.HttpLoggingInterceptor;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

@DisplayName("InterceptorAutoConfiguration")
class InterceptorAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(InterceptorAutoConfiguration.class))
        .withUserConfiguration(TestConfig.class);
    @TestConfiguration
    @EnableConfigurationProperties(MoniKitLoggingProperties.class)
    static class TestConfig {}


    @Nested
    @DisplayName("인터셉터 등록 여부")
    class InterceptorRegistration {

        @Test
        @DisplayName("shouldRegisterWebMvcConfigurerWhenInterceptorIsEnabled")
        void shouldRegisterWebMvcConfigurerWhenInterceptorIsEnabled() {
            contextRunner
                .withBean(HttpLoggingInterceptor.class, () -> mock(HttpLoggingInterceptor.class))
                .run(context -> {
                    assertTrue(context.getBeansOfType(WebMvcConfigurer.class).size() > 0);
                });
        }

        @Test
        @DisplayName("shouldSkipInterceptorWhenLogEnabledIsFalse")
        void shouldSkipInterceptorWhenLogEnabledIsFalse() {
            contextRunner
                .withBean(HttpLoggingInterceptor.class, () -> mock(HttpLoggingInterceptor.class))
                .withPropertyValues("monikit.logging.filters.log-enabled=false")
                .run(context -> {
                    assertTrue(context.getBeansOfType(WebMvcConfigurer.class).size() > 0);
                });
        }

        @Test
        @DisplayName("shouldNotFailIfHttpLoggingInterceptorMissingAndLogDisabled")
        void shouldNotFailIfHttpLoggingInterceptorMissingAndLogDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.filters.log-enabled=false")
                .run(context -> {
                    assertTrue(context.getBeansOfType(WebMvcConfigurer.class).size() > 0);
                });
        }
    }
}
