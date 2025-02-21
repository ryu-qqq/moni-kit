package com.monikit.starter.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.core.DataSourceProvider;
import com.monikit.starter.DefaultDataSourceProvider;
import com.monikit.starter.LoggingDataSource;
import com.monikit.starter.LoggingPreparedStatementFactory;

/**
 * 데이터소스 로깅 및 데이터소스 프로바이더 자동 설정 클래스.
 * <p>
 * - `LoggingDataSource`를 자동으로 감싸서 적용.
 * - `DataSourceProvider`가 없을 경우 `DefaultDataSourceProvider`를 자동으로 사용.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
@Configuration
public class DataSourceLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoggingConfig.class);


    /**
     * 사용자가 `DataSource`를 별도로 등록하지 않으면 `LoggingDataSource`를 자동 적용함.
     * <p>
     * - 기존 `DataSource`를 감싸서 SQL 실행 로깅이 가능하도록 함.
     * - SQL 실행 시 `LoggingPreparedStatementFactory`를 활용하여 로깅을 수행.
     * </p>
     */
    @Bean
    @Primary
    public DataSource loggingDataSource(DataSource originalDataSource,
                                        LoggingPreparedStatementFactory preparedStatementFactory) {
        logger.info("✅ Using DataSource: {}", originalDataSource.getClass().getSimpleName());
        DataSource loggingDataSource = new LoggingDataSource(originalDataSource, preparedStatementFactory);
        logger.info("🔄 Wrapped DataSource with LoggingDataSource: {}", loggingDataSource.getClass().getSimpleName());
        return loggingDataSource;
    }

    /**
     * 사용자가 `DataSourceProvider`를 직접 구현하지 않은 경우, 기본 `DefaultDataSourceProvider`를 사용.
     * <p>
     * - 데이터소스가 설정되지 않으면 기본적으로 `"unknownDataSource"` 반환.
     * - 최초 한 번만 커넥션을 조회하고, 이후에는 캐싱된 값을 사용하여 성능 최적화.
     * </p>
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceProvider.class)
    public DataSourceProvider defaultDataSourceProvider(ObjectProvider<DataSource> dataSourceProvider) {
        return new DefaultDataSourceProvider(dataSourceProvider);
    }

}