package com.monikit.starter.jdbc.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.TraceIdProvider;
import com.monikit.starter.jdbc.DataSourceProvider;
import com.monikit.starter.jdbc.QueryLoggingService;
import com.monikit.starter.jdbc.logging.DefaultQueryLoggingService;
import com.monikit.starter.jdbc.proxy.LoggingPreparedStatementFactory;


/**
 * {@link QueryLoggingService} 및 {@link LoggingPreparedStatementFactory}를 자동 구성하는 설정 클래스.
 *
 * <p>
 * - SQL 실행 정보를 기록하는 {@link QueryLoggingService}를 조건부로 등록함.
 * - {@link LoggingPreparedStatementFactory}는 항상 등록되며 쿼리 로깅을 위한 프록시 생성 역할을 담당.
 * </p>
 *
 * <h3>조건</h3>
 * <ul>
 *     <li>{@code QueryLoggingService}가 빈으로 존재하지 않을 경우에만 {@link DefaultQueryLoggingService}가 등록됨</li>
 * </ul>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Configuration
public class QueryLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(QueryLoggingConfig.class);

    /**
     * {@link QueryLoggingService}의 기본 구현체를 조건부로 등록.
     *
     * @param logEntryContextManager 로그 저장소 (ex. ThreadLocal 기반)
     * @param dataSourceProvider     데이터소스 이름을 제공하는 컴포넌트
     * @param moniKitLoggingProperties 로깅에 사용될 설정 정보
     * @return {@link DefaultQueryLoggingService}
     */
    @Bean
    @ConditionalOnMissingBean(QueryLoggingService.class)
    public QueryLoggingService queryLoggingService(
        LogEntryContextManager logEntryContextManager,
        DataSourceProvider dataSourceProvider,
        MoniKitLoggingProperties moniKitLoggingProperties
    ) {
        logger.info("[MoniKit] Creating QueryLoggingService bean...");
        logger.info("[MoniKit] Using LogEntryContextManager: {}", logEntryContextManager.getClass().getSimpleName());
        logger.info("[MoniKit] Using DataSourceProvider: {}", dataSourceProvider.getClass().getSimpleName());
        logger.info("[MoniKit] MoniKit Logging Properties: SlowQueryThreshold={}, CriticalQueryThreshold={}",
            moniKitLoggingProperties.getSlowQueryThresholdMs(),
            moniKitLoggingProperties.getCriticalQueryThresholdMs());

        return new DefaultQueryLoggingService(
            logEntryContextManager,
            dataSourceProvider,
            moniKitLoggingProperties.getSlowQueryThresholdMs(),
            moniKitLoggingProperties.getCriticalQueryThresholdMs()
        );
    }

    /**
     * {@link LoggingPreparedStatementFactory}를 항상 등록한다.
     *
     * <p>
     * 쿼리 실행을 감싸는 프록시 {@link com.monikit.starter.jdbc.proxy.LoggingPreparedStatement} 생성을 담당.
     * </p>
     *
     * @param queryLoggingService 쿼리 로깅 서비스
     * @return {@link LoggingPreparedStatementFactory}
     */
    @Bean
    public LoggingPreparedStatementFactory loggingPreparedStatementFactory(QueryLoggingService queryLoggingService, TraceIdProvider traceIdProvider) {
        return new LoggingPreparedStatementFactory(queryLoggingService, traceIdProvider);
    }

}