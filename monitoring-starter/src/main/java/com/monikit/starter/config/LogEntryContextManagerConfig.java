package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DefaultErrorLogNotifier;
import com.monikit.core.DefaultLogEntryContextManager;
import com.monikit.core.DefaultLogNotifier;
import com.monikit.core.DefaultThreadContextHandler;
import com.monikit.core.ErrorLogNotifier;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.LogNotifier;
import com.monikit.core.ThreadContextHandler;

/**
 * `LogEntryContextManager` 및 관련 컴포넌트들을 Spring 빈으로 등록하는 설정 클래스.
 * <p>
 * - 사용자가 직접 정의한 빈이 있으면 해당 클래스명을 로그로 출력.
 * - 기본적으로 `Default` 구현체들을 자동 등록하지만, 사용자 정의 빈이 있으면 이를 우선 사용함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class LogEntryContextManagerConfig {

    private static final Logger logger = LoggerFactory.getLogger(LogEntryContextManagerConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    /**
     * `LogNotifier` 빈을 등록하며, 사용자가 별도로 정의한 경우 해당 클래스를 로그로 남김.
     */
    @Bean
    @ConditionalOnMissingBean(LogNotifier.class)
    public LogNotifier defaultLogNotifier() {
        LogNotifier logNotifier = DefaultLogNotifier.getInstance();
        logger.info("LogNotifier Bean Registered (Default): {}", logNotifier.getClass().getSimpleName());
        return logNotifier;
    }

    @Bean
    public void logRegisteredLogNotifier() {
        LogNotifier logNotifier = applicationContext.getBean(LogNotifier.class);
        logger.info("Using LogNotifier Bean: {}", logNotifier.getClass().getSimpleName());
    }

    /**
     * `ErrorLogNotifier` 빈을 등록하며, 사용자가 별도로 정의한 경우 해당 클래스를 로그로 남김.
     */
    @Bean
    @ConditionalOnMissingBean(ErrorLogNotifier.class)
    public ErrorLogNotifier defaultErrorLogNotifier() {
        ErrorLogNotifier errorLogNotifier = DefaultErrorLogNotifier.getInstance();
        logger.info("ErrorLogNotifier Bean Registered (Default): {}", errorLogNotifier.getClass().getSimpleName());
        return errorLogNotifier;
    }

    @Bean
    public void logRegisteredErrorLogNotifier() {
        ErrorLogNotifier errorLogNotifier = applicationContext.getBean(ErrorLogNotifier.class);
        logger.info(" Using ErrorLogNotifier Bean: {}", errorLogNotifier.getClass().getSimpleName());
    }

    /**
     * `LogEntryContextManager` 빈을 등록하며, 사용자가 직접 정의한 경우 해당 클래스를 로그로 남김.
     */
    @Bean
    @ConditionalOnMissingBean(LogEntryContextManager.class)
    public LogEntryContextManager logEntryContextManager(LogNotifier logNotifier, ErrorLogNotifier errorLogNotifier) {
        LogEntryContextManager logEntryContextManager = new DefaultLogEntryContextManager(logNotifier, errorLogNotifier);
        logger.info("LogEntryContextManager Bean Registered (Default): {}", logEntryContextManager.getClass().getSimpleName());
        return logEntryContextManager;
    }

    @Bean
    public void logRegisteredLogEntryContextManager() {
        LogEntryContextManager logEntryContextManager = applicationContext.getBean(LogEntryContextManager.class);
        logger.info("Using LogEntryContextManager Bean: {}", logEntryContextManager.getClass().getSimpleName());
    }

    /**
     * `ThreadContextHandler` 빈을 등록하며, 사용자가 직접 정의한 경우 해당 클래스를 로그로 남김.
     */
    @Bean
    @ConditionalOnMissingBean(ThreadContextHandler.class)
    public ThreadContextHandler threadContextHandler() {
        ThreadContextHandler threadContextHandler = new DefaultThreadContextHandler();
        logger.info("ThreadContextHandler Bean Registered (Default): {}", threadContextHandler.getClass().getSimpleName());
        return threadContextHandler;
    }

    @Bean
    public void logRegisteredThreadContextHandler() {
        ThreadContextHandler threadContextHandler = applicationContext.getBean(ThreadContextHandler.class);
        logger.info("Using ThreadContextHandler Bean: {}", threadContextHandler.getClass().getSimpleName());
    }

}