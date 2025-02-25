package com.monikit.starter.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultLogEntryContextManager;
import com.monikit.core.DefaultThreadContextHandler;
import com.monikit.core.ErrorLogNotifier;
import com.monikit.core.LogEntry;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogNotifier;
import com.monikit.core.MetricCollector;
import com.monikit.core.ThreadContextHandler;

/**
 * `LogEntryContextManager` 및 관련 컴포넌트들을 Spring 빈으로 등록하는 설정 클래스.
 * <p>
 * - 로그 수집 및 모니터링을 위한 핵심 컴포넌트들을 자동 등록.
 * - `MetricCollector`를 활용하여 로그 수집 시 관련 메트릭을 자동으로 기록하도록 개선됨.
 * - 사용자가 별도의 구현체를 등록하지 않으면 기본값(`DefaultLogEntryContextManager`, `DefaultThreadContextHandler`)을 자동으로 사용.
 * - 멀티스레드 환경에서 로그 컨텍스트를 전파할 수 있도록 `ThreadContextHandler`를 제공.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */
@Configuration
public class LogEntryContextManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(LogEntryContextManagerConfig.class);

    /**
     * `LogEntryContextManager` 빈을 등록.
     * <p>
     * - `LogNotifier`, `ErrorLogNotifier`, `MetricCollector` 리스트를 자동으로 주입받아 사용.
     * - `MetricCollector`를 활용하여 로그 수집 시 관련 메트릭을 자동 기록하도록 개선됨.
     * - 사용자가 별도의 `LogEntryContextManager` 빈을 제공하지 않을 경우 `DefaultLogEntryContextManager`를 기본값으로 사용.
     * - 등록 시 로깅(`logger.info()`)을 통해 어떤 구현체가 등록되었는지 확인 가능.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(LogEntryContextManager.class)
    public LogEntryContextManager logEntryContextManager(LogNotifier logNotifier, ErrorLogNotifier errorLogNotifier, List<MetricCollector<? extends LogEntry>> metricCollectors) {
        LogEntryContextManager logEntryContextManager = new DefaultLogEntryContextManager(logNotifier, errorLogNotifier, metricCollectors);
        logger.info("LogEntryContextManager Bean Registered: {}", logEntryContextManager.getClass().getSimpleName());
        return logEntryContextManager;
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