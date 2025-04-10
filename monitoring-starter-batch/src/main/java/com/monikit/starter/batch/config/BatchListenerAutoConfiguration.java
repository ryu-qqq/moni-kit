package com.monikit.starter.batch.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.batch.DefaultJobExecutionListener;
import com.monikit.starter.batch.DefaultStepExecutionListener;

/**
 * Spring Batch용 기본 Job/Step 리스너를 자동 등록하는 설정 클래스.
 * <p>
 * - 로그 수집 및 메트릭 처리를 위한 기본 Listener log-enabled 가 true 일때  자동 등록합니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */
@Configuration
public class BatchListenerAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(BatchListenerAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean(DefaultJobExecutionListener.class)
    @ConditionalOnProperty(prefix = "monikit.logging", name = "log-enabled", havingValue = "true", matchIfMissing = false)
    public JobExecutionListener jobExecutionListener(
        LogEntryContextManager contextManager,
        TraceIdProvider traceIdProvider
    ) {
        logger.info("[MoniKit] DefaultJobExecutionListener registered");
        return new DefaultJobExecutionListener(traceIdProvider, contextManager);
    }

    @Bean
    @ConditionalOnMissingBean(DefaultStepExecutionListener.class)
    @ConditionalOnProperty(prefix = "monikit.logging", name = "log-enabled", havingValue = "true", matchIfMissing = false)
    public StepExecutionListener stepExecutionListener(
        LogEntryContextManager contextManager,
        TraceIdProvider traceIdProvider
    ) {
        logger.info("[MoniKit] DefaultStepExecutionListener registered");
        return new DefaultStepExecutionListener(traceIdProvider, contextManager);
    }
}