package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.ThreadContextHandler;
import com.monikit.starter.MDCThreadContextHandler;



/**
 * {@link ThreadContextHandler}의 기본 구현체로
 * {@link MDCThreadContextHandler}를 자동 등록하는 설정 클래스입니다.
 *
 * <p>
 * - 사용자가 별도로 {@code ThreadContextHandler} 빈을 등록하지 않은 경우에만 동작합니다.
 * - SLF4J MDC를 기반으로, traceId 및 로그 컨텍스트를 자식 스레드로 안전하게 전파합니다.
 * - {@link LogEntryContextManager}를 함께 사용하여 요청 단위 로그 수집 컨텍스트까지 유지합니다.
 * </p>
 *
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
@Configuration
@ConditionalOnMissingBean(ThreadContextHandler.class)
public class ThreadContextHandlerAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(ThreadContextHandlerAutoConfiguration.class);

    @Bean
    public ThreadContextHandler threadContextHandler(LogEntryContextManager logEntryContextManager) {
        logger.info("[MoniKit] No custom ThreadContextHandler found. Using default MDCThreadContextHandler.");
        return new MDCThreadContextHandler(logEntryContextManager);
    }
}