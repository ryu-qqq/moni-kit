package com.monikit.starter.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultLogEntryContextManager;
import com.monikit.core.LogAddHook;
import com.monikit.core.LogAddHookCustomizer;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogFlushHook;
import com.monikit.core.LogFlushHookCustomizer;
import com.monikit.core.LogNotifier;

import jakarta.annotation.Nullable;

/**
 * `LogEntryContextManager` 및 관련 컴포넌트들을 Spring 빈으로 등록하는 설정 클래스.
 * <p>
 * - 로그 수집 및 후처리를 위한 핵심 컴포넌트들을 자동 등록합니다.
 * - `LogAddHook`, `LogFlushHook` 을 통해 확장 가능한 후처리 기능을 제공합니다.
 * - 사용자가 별도의 구현체를 등록하지 않을 경우 기본값(`DefaultLogEntryContextManager`, `DefaultThreadContextHandler`)을 자동으로 사용합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Configuration
public class LogEntryContextManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(LogEntryContextManagerConfig.class);

    /**
     * `LogEntryContextManager` 빈을 등록합니다.
     * <p>
     * - `LogNotifier`, `LogAddHook`, `LogFlushHook` 을 자동 주입받아 구성합니다.
     * - 사용자가 별도로 `LogEntryContextManager` 빈을 등록하지 않을 경우 기본 구현체가 사용됩니다.
     * </p>
     */

    @Bean
    @ConditionalOnMissingBean(LogEntryContextManager.class)
    public LogEntryContextManager logEntryContextManager(
        LogNotifier logNotifier,
        @Nullable List<LogAddHook> addHooks,
        @Nullable List<LogAddHookCustomizer> addHookCustomizers,
        @Nullable List<LogFlushHookCustomizer> flushHookCustomizers
    ) {
        List<LogAddHook> finalAddHooks = new ArrayList<>(addHooks != null ? addHooks : List.of());
        if (addHookCustomizers != null) {
            for (LogAddHookCustomizer customizer : addHookCustomizers) {
                customizer.customize(finalAddHooks);
            }
            logger.info("[MoniKit] Registering LogAddHookCustomizer with {} collector(s)", addHookCustomizers.size());

        }

        List<LogFlushHook> finalFlushHooks = new ArrayList<>();
        if (flushHookCustomizers != null) {
            for (LogFlushHookCustomizer customizer : flushHookCustomizers) {
                customizer.customize(finalFlushHooks);
            }
            logger.info("[MoniKit] Registering LogFlushHookCustomizer with {} collector(s)", flushHookCustomizers.size());
        }

        return new DefaultLogEntryContextManager(logNotifier, finalAddHooks, finalFlushHooks);
    }


}