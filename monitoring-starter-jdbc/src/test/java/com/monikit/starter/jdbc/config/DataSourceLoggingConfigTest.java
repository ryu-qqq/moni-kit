package com.monikit.starter.jdbc.config;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.monikit.config.MoniKitLoggingProperties;
import com.monikit.starter.jdbc.LoggingDataSource;
import com.monikit.starter.jdbc.proxy.LoggingPreparedStatementFactory;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("DataSourceLoggingConfig 테스트")
class DataSourceLoggingConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(DataSourceLoggingConfig.class))
        .withUserConfiguration(TestConfig.class)
        .withBean(LoggingPreparedStatementFactory.class, () -> new LoggingPreparedStatementFactory(null, null))
        .withBean("originalDataSource", DataSource.class, FakeDataSource::new);

    @TestConfiguration
    @EnableConfigurationProperties(MoniKitLoggingProperties.class)
    static class TestConfig {}

    @Nested
    @DisplayName("DataSource Wrapping 테스트")
    class DataSourceWrappingTests {

        @Test
        @DisplayName("logEnabled=false일 때 LoggingDataSource로 감싸지지 않아야 한다")
        void shouldNotWrapWithLoggingDataSourceWhenLogDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=false",
                    "monikit.logging.datasource-logging-enabled=true") // datasourceLoggingEnabled가 true지만 logEnabled가 false
                .run(context -> {
                    DataSource dataSource = context.getBean(DataSource.class);
                    assertNotNull(dataSource);
                    assertFalse(dataSource instanceof LoggingDataSource, "logEnabled가 false면 LoggingDataSource가 적용되지 않아야 한다.");
                });
        }

        @Test
        @DisplayName("datasourceLoggingEnabled=false일 때 LoggingDataSource로 감싸지지 않아야 한다")
        void shouldNotWrapWithLoggingDataSourceWhenDatasourceLoggingDisabled() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=true",
                    "monikit.logging.datasource-logging-enabled=false") // logEnabled가 true여도 datasourceLoggingEnabled가 false면 감싸지지 않음
                .run(context -> {
                    DataSource dataSource = context.getBean(DataSource.class);
                    assertNotNull(dataSource);
                    assertFalse(dataSource instanceof LoggingDataSource, "datasourceLoggingEnabled가 false면 LoggingDataSource가 적용되지 않아야 한다.");
                });
        }

        @Test
        @DisplayName("logEnabled=true && datasourceLoggingEnabled=true일 때 LoggingDataSource로 감싸져야 한다")
        void shouldWrapWithLoggingDataSourceWhenBothEnabled() {
            contextRunner
                .withPropertyValues("monikit.logging.log-enabled=true",
                    "monikit.logging.datasource-logging-enabled=true") // 두 값이 모두 true일 때만 감싸야 함
                .run(context -> {
                    DataSource dataSource = context.getBean(DataSource.class);
                    assertNotNull(dataSource);
                    Assertions.assertInstanceOf(LoggingDataSource.class, dataSource);
                });
        }
    }

    /** 테스트용 Fake DataSource */
    static class FakeDataSource implements DataSource {
        @Override public java.sql.Connection getConnection() { return null; }
        @Override public java.sql.Connection getConnection(String username, String password) { return null; }
        @Override public java.io.PrintWriter getLogWriter() { return null; }
        @Override public void setLogWriter(java.io.PrintWriter out) {}
        @Override public void setLoginTimeout(int seconds) {}
        @Override public int getLoginTimeout() { return 0; }
        @Override public java.util.logging.Logger getParentLogger() { return null; }
        @Override public <T> T unwrap(Class<T> iface) { return null; }
        @Override public boolean isWrapperFor(Class<?> iface) { return false; }
    }
}