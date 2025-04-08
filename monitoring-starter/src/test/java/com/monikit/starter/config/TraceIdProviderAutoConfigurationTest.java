package com.monikit.starter.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.core.DefaultTraceIdProvider;
import com.monikit.core.TraceIdProvider;

import static org.junit.jupiter.api.Assertions.*;

class TraceIdProviderAutoConfigurationTest {

    private final ApplicationContextRunner contextRunner =
        new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(TraceIdProviderAutoConfiguration.class));

    @Test
    @DisplayName("shouldRegisterDefaultTraceIdProviderWhenNoCustomBean")
    void shouldRegisterDefaultTraceIdProviderWhenNoCustomBean() {
        contextRunner.run(context -> {
            assertNotNull(context.getBean(TraceIdProvider.class));
            assertEquals(DefaultTraceIdProvider.class, context.getBean(TraceIdProvider.class).getClass());
        });
    }

}