package com.monikit.starter.config;

import jakarta.annotation.Nullable;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.LogNotifier;
import com.monikit.core.LogSink;
import com.monikit.core.LogSinkCustomizer;

@Configuration
@ConditionalOnMissingBean(LogNotifier.class)
public class Slf4jLoggerAutoConfiguration {

    @Bean
    public LogNotifier slf4jLogNotifier(List<LogSink> sinks,
                                        @Nullable List<LogSinkCustomizer> customizers) {
        if (customizers != null) {
            for (LogSinkCustomizer customizer : customizers) {
                customizer.customize(sinks);
            }
        }

        return new Slf4jLogger(sinks);
    }
}