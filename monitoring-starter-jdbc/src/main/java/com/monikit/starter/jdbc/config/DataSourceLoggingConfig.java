package com.monikit.starter.jdbc.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.starter.jdbc.DataSourceProvider;
import com.monikit.starter.jdbc.DefaultDataSourceProvider;
import com.monikit.starter.jdbc.LoggingDataSource;
import com.monikit.starter.jdbc.proxy.LoggingPreparedStatementFactory;

/**
 * {@link LoggingDataSource} 및 {@link DataSourceProvider}를 자동 구성하는 Spring Boot 설정 클래스.
 *
 * <p>
 * 이 클래스는 모니터링을 위한 JDBC 커넥션 감시 기능을 활성화하며, 다음과 같은 조건에 따라 동작한다.
 * </p>
 *
 * <ul>
 *     <li>{@code monikit.logging.log-enabled=true}</li>
 *     <li>{@code monikit.logging.datasource-logging-enabled=true}</li>
 * </ul>
 *
 * <p>
 * 위 조건이 만족되면, 기존 {@code DataSource}를 감싼 프록시 {@link LoggingDataSource}가 생성되어 쿼리 추적 기능을 수행한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Configuration
@EnableConfigurationProperties({MoniKitLoggingProperties.class})
public class DataSourceLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoggingConfig.class);

    /**
     * 기존 {@code DataSource}를 {@link LoggingDataSource}로 감싸는 빈.
     *
     * <p>
     * - {@code "originalDataSource"}라는 이름으로 DataSource가 등록되어 있어야 함
     * - {@code logEnabled=true} && {@code datasourceLoggingEnabled=true}일 경우에만 적용
     * </p>
     *
     * @param originalDataSource 원본 데이터소스
     * @param preparedStatementFactory PreparedStatement 프록시 생성 팩토리
     * @param loggingProperties 로깅 속성
     * @return 로깅이 적용된 {@code DataSource} 또는 원본 {@code DataSource}
     */

    @Bean
    @Primary
    @ConditionalOnBean(name = "originalDataSource")
    public DataSource loggingDataSource(
        @Qualifier("originalDataSource") DataSource originalDataSource,
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
        return new LoggingDataSource(originalDataSource, preparedStatementFactory);
    }

    /**
     * {@link DataSourceProvider}가 없을 경우 기본 구현체를 등록한다.
     *
     * @param dataSourceProvider {@code DataSource}의 ObjectProvider
     * @return 기본 {@link DefaultDataSourceProvider}
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceProvider.class)
    public DataSourceProvider defaultDataSourceProvider(ObjectProvider<DataSource> dataSourceProvider) {
        logger.info("No custom DataSourceProvider found. Using DefaultDataSourceProvider.");
        return new DefaultDataSourceProvider(dataSourceProvider);
    }


}