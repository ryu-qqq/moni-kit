package com.monikit.starter.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultLogEntryContextManager;
import com.monikit.core.DefaultThreadContextHandler;
import com.monikit.core.LogAddHook;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogFlushHook;
import com.monikit.core.LogNotifier;
import com.monikit.core.ThreadContextHandler;

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
    public LogEntryContextManager logEntryContextManager(LogNotifier logNotifier,
                                                         List<LogAddHook> addHooks,
                                                         List<LogFlushHook> flushHooks) {

        LogEntryContextManager contextManager = new DefaultLogEntryContextManager(logNotifier, addHooks, flushHooks);

        logger.info("[MoniKit] LogEntryContextManager Bean Registered: {}", contextManager.getClass().getSimpleName());
        logger.info("[MoniKit] LogAddHooks Registered: {}", addHooks.stream().map(Object::getClass).map(Class::getSimpleName).toList());
        logger.info("[MoniKit] LogFlushHooks Registered: {}", flushHooks.stream().map(Object::getClass).map(Class::getSimpleName).toList());

        return contextManager;
    }


    /**
     * `ThreadContextHandler` 빈을 등록.
     * <p>
     * - 멀티스레드 환경에서 부모 스레드의 로그 컨텍스트를 자식 스레드로 전파하는 역할.
     * - 기본적으로 `DefaultThreadContextHandler`를 사용하지만, 사용자가 직접 커스텀 구현체를 등록할 수 있음.
     * - `MDC`와 같은 추가적인 컨텍스트를 함께 전파하려면 `ThreadContextHandler`를 상속받아 별도 구현 가능.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(ThreadContextHandler.class)
    public ThreadContextHandler threadContextHandler() {
        ThreadContextHandler threadContextHandler = new DefaultThreadContextHandler();
        logger.info("ThreadContextHandler Bean Registered: {}", threadContextHandler.getClass().getSimpleName());
        return threadContextHandler;
    }

}