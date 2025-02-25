package com.monikit.starter.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
 * - `logEnabled=true` && `datasourceLoggingEnabled=true`일 경우 `LoggingDataSource`를 감싸도록 적용.
 * - `DataSourceProvider`가 없을 경우 `DefaultDataSourceProvider`를 자동으로 사용.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */
@Configuration
@EnableConfigurationProperties({MoniKitLoggingProperties.class})
public class DataSourceLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoggingConfig.class);

    /**
     * 사용자가 `DataSource`를 별도로 등록하지 않으면 `LoggingDataSource`를 자동 적용함.
     * <p>
     * - `logEnabled=true` && `datasourceLoggingEnabled=true`일 때만 적용됨.
     * - SQL 실행 시 `LoggingPreparedStatementFactory`를 활용하여 로깅을 수행.
     * </p>
     */
    @Bean
    @Primary
    public DataSource loggingDataSource(DataSource originalDataSource,
                                        LoggingPreparedStatementFactory preparedStatementFactory,
                                        MoniKitLoggingProperties loggingProperties) {
        if (!loggingProperties.isLogEnabled()) {
            logger.warn("logEnabled is disabled. Returning original DataSource.");
            return originalDataSource;
        }

        if (!loggingProperties.isDatasourceLoggingEnabled()) {
            logger.info("Datasource logging is disabled. Returning original DataSource.");
            return originalDataSource;
        }

        logger.info("Datasource logging is enabled. Wrapping DataSource with LoggingDataSource.");
        DataSource loggingDataSource = new LoggingDataSource(originalDataSource, preparedStatementFactory);
        logger.info("Wrapped DataSource with LoggingDataSource: {}", loggingDataSource.getClass().getSimpleName());
        return loggingDataSource;
    }

    /**
     * 사용자가 `DataSourceProvider`를 직접 구현하지 않은 경우, 기본 `DefaultDataSourceProvider`를 사용.
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceProvider.class)
    public DataSourceProvider defaultDataSourceProvider(ObjectProvider<DataSource> dataSourceProvider) {
        logger.info("No custom DataSourceProvider found. Using Default DataSourceProvider: {}", dataSourceProvider.getClass().getSimpleName());
        return new DefaultDataSourceProvider(dataSourceProvider);
    }
}