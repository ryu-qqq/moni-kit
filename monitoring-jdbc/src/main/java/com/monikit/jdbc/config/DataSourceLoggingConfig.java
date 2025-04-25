package com.monikit.jdbc.config;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.jdbc.DataSourceProvider;
import com.monikit.jdbc.DefaultDataSourceProvider;
import com.monikit.jdbc.LoggingDataSource;
import com.monikit.jdbc.proxy.LoggingPreparedStatementFactory;


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
@ConditionalOnProperty(
    prefix = "monikit.logging",
    name = "datasource-logging-enabled",
    havingValue = "true",
    matchIfMissing = false
)
public class DataSourceLoggingConfig {

    private static final Logger logger = LoggerFactory.getLogger(DataSourceLoggingConfig.class);

    /**
     * {@code DataSource}를 {@link LoggingDataSource}로 감싸는 프록시 빈을 등록합니다.
     *
     * <p>
     * 단, {@code monikit.logging.log-enabled=true}일 때만 래핑이 적용되며,
     * 그렇지 않으면 원본 {@code DataSource}를 그대로 반환합니다.
     * </p>
     *
     * @param dataSourceProvider {@code DataSource} 주입 제공자
     * @param preparedStatementFactory 프록시 팩토리
     * @param loggingProperties 설정
     * @return 래핑된 또는 원본 {@code DataSource}
     */
    @Bean
    @Primary
    public DataSource loggingDataSource(
        ObjectProvider<DataSource> dataSourceProvider,
        LoggingPreparedStatementFactory preparedStatementFactory,
        MoniKitLoggingProperties loggingProperties
    ) {
        DataSource original = dataSourceProvider.getIfAvailable();
        if (original == null) {
            logger.warn("[MoniKit] No DataSource found. Cannot wrap.");
            return null;
        }

        if (!loggingProperties.isLogEnabled()) {
            logger.info("[MoniKit] logEnabled is false. Returning original DataSource.");
            return original;
        }

        logger.info("[MoniKit] Wrapping DataSource with LoggingDataSource.");
        return new LoggingDataSource(original, preparedStatementFactory);
    }

    /**
     * {@link DataSourceProvider}가 존재하지 않을 경우 기본 구현체를 등록합니다.
     *
     * @param dataSourceProvider {@code DataSource}의 Provider
     * @return {@link DefaultDataSourceProvider}
     */
    @Bean
    @ConditionalOnMissingBean(DataSourceProvider.class)
    public DataSourceProvider defaultDataSourceProvider(ObjectProvider<DataSource> dataSourceProvider) {
        logger.info("[MoniKit] Registering DefaultDataSourceProvider.");
        return new DefaultDataSourceProvider(dataSourceProvider);
    }


}