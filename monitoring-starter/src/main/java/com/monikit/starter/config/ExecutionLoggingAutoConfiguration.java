package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.ExecutionLoggingAspect;

/**
 * 메서드 실행 시간 및 상세 로깅을 위한 AOP 자동 구성 클래스.
 * <p>
 * - monikit.logging.detailed-logging=true일 때만 AOP를 활성화함.
 * - 서비스 및 리포지토리 클래스의 메서드 실행 시간을 측정하여 로그로 기록.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Configuration
@ConditionalOnProperty(name = "monikit.logging.logging-enabled", havingValue = "true", matchIfMissing = false)
public class ExecutionLoggingAutoConfiguration {


    private static final Logger logger = LoggerFactory.getLogger(ExecutionLoggingAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ExecutionLoggingAspect executionLoggingAspect(LogEntryContextManager logEntryContextManager,
                                                         MoniKitLoggingProperties loggingProperties,
                                                         TraceIdProvider traceIdProvider) {
        logger.info("[MoniKit] ExecutionLoggingAspect Registered");
        return new ExecutionLoggingAspect(logEntryContextManager, loggingProperties, traceIdProvider);
    }

}
