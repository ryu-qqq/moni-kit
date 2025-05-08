package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.starter.DynamicMatcher;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExecutionLoggingAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(ExecutionLoggingAutoConfiguration.class))
        .withPropertyValues("monikit.logging.log-enabled=true");


    @Test
    @DisplayName("shouldRegisterDynamicMatcherWhenLogEnabledIsTrue")
    void shouldRegisterDynamicMatcherWhenLogEnabledIsTrue() {
        contextRunner
            .withPropertyValues("monikit.logging.log-enabled=true")
            .run(context -> {
                assertTrue(context.containsBean("dynamicMatcher"));
                assertNotNull(context.getBean(DynamicMatcher.class));
            });
    }

    @Test
    @DisplayName("shouldNotRegisterDynamicMatcherWhenLogEnabledIsFalse")
    void shouldNotRegisterDynamicMatcherWhenLogEnabledIsFalse() {
        contextRunner
            .withPropertyValues("monikit.logging.log-enabled=false")
            .run(context -> assertFalse(context.containsBean("dynamicMatcher")));
    }

    @Test
    @DisplayName("shouldNotRegisterDynamicMatcherWhenPropertyIsMissing")
    void shouldNotRegisterDynamicMatcherWhenPropertyIsMissing() {
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ExecutionLoggingAutoConfiguration.class))
            .run(context -> assertFalse(context.containsBean("dynamicMatcher")));
    }
}
