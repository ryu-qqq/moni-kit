package com.monikit.starter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.stereotype.Component;

import com.monikit.core.DataSourceProvider;

/**
 * 기본 `DataSourceProvider` 구현 (데이터소스를 자동 감지하여 반환).
 * <p>
 * - HikariCP 등의 커넥션 풀을 감지하여 데이터소스 이름 반환.
 * - 인메모리(Embedded) DB의 경우 `"embeddedDatabase"` 반환.
 * - 감지할 수 없는 경우 `"unknownDataSource"` 반환.
 * - **최초 한 번만 조회하여 캐싱하고 이후 호출 시 재사용 (불필요한 커넥션 생성 방지)**
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1
 */
@Component
public class DefaultDataSourceProvider implements DataSourceProvider {

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final AtomicReference<String> cachedDatabaseName = new AtomicReference<>(null);

    public DefaultDataSourceProvider(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    @Override
    public String getDataSourceName() {
        if (cachedDatabaseName.get() != null) {
            return cachedDatabaseName.get();
        }

        String dbName = fetchDatabaseName();
        cachedDatabaseName.set(dbName);
        return dbName;
    }

    /**
     * 데이터소스에서 JDBC URL을 조회하고, 데이터베이스 이름을 추출한다.
     */
    private String fetchDatabaseName() {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            return "unknownDataSource";
        }

        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            return extractDatabaseNameFromUrl(url);
        } catch (SQLException e) {
            return "unknownDataSource";
        }
    }

    /**
     * JDBC URL에서 데이터베이스 이름 추출
     *
     * @param url JDBC URL
     * @return 데이터베이스 이름 (없을 경우 `"unknownDataSource"`)
     */
    private String extractDatabaseNameFromUrl(String url) {
        if (url == null) {
            return "unknownDataSource";
        }

        if (url.startsWith("jdbc:mysql://") || url.startsWith("jdbc:postgresql://") || url.startsWith("jdbc:mariadb://")) {
            String[] parts = url.split("/");
            return parts.length > 3 ? parts[3].split("\\?")[0] : "unknownDataSource";
        }

        // H2 또는 기타 인메모리 데이터베이스
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource instanceof EmbeddedDatabase) {
            return "embeddedDatabase";
        }

        return "unknownDataSource";
    }
}