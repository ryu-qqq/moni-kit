package com.monikit.starter.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.core.DataSourceProvider;
import com.monikit.core.LogEntryContextManager;
import com.monikit.core.MetricCollector;
import com.monikit.core.QueryLoggingService;
import com.monikit.core.DefaultQueryLoggingService;

/**
 * `QueryLoggingService`를 빈으로 등록하는 설정 클래스.
 * <p>
 * - SQL 실행 로그를 기록하고, 메트릭을 수집하는 `QueryLoggingService`를 등록.
 * - 사용자가 별도의 구현체를 제공하지 않을 경우 `DefaultQueryLoggingService`를 기본값으로 사용.
 * - 데이터소스, 메트릭 수집기, 로깅 속성 정보를 자동으로 주입하여 구성.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class QueryLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(QueryLoggingConfig.class);

    /**
     * `QueryLoggingService`를 빈으로 등록.
     * <p>
     * - Spring 컨텍스트에서 `QueryLoggingService` 빈이 존재하지 않을 경우 자동으로 등록됨.
     * - `LogEntryContextManager`, `MetricCollector`, `DataSourceProvider`를 주입받아 초기화.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(QueryLoggingService.class)
    public QueryLoggingService queryLoggingService(
        LogEntryContextManager logEntryContextManager,
        DataSourceProvider dataSourceProvider,
        MoniKitLoggingProperties moniKitLoggingProperties
    ) {
        logger.info("Creating QueryLoggingService bean...");
        logger.info("Using LogEntryContextManager: {}", logEntryContextManager.getClass().getSimpleName());
        logger.info("Using DataSourceProvider: {}", dataSourceProvider.getClass().getSimpleName());
        logger.info("MoniKit Logging Properties: SlowQueryThreshold={}, CriticalQueryThreshold={}",
            moniKitLoggingProperties.getSlowQueryThresholdMs(),
            moniKitLoggingProperties.getCriticalQueryThresholdMs());

        QueryLoggingService queryLoggingService = new DefaultQueryLoggingService(
            logEntryContextManager,
            dataSourceProvider,
            moniKitLoggingProperties.getSlowQueryThresholdMs(),
            moniKitLoggingProperties.getCriticalQueryThresholdMs()
        );

        logger.info("Successfully created QueryLoggingService: {}", queryLoggingService.getClass().getSimpleName());

        return queryLoggingService;
    }

}