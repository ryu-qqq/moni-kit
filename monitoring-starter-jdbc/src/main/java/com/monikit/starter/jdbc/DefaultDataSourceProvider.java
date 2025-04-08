package com.monikit.starter.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.stereotype.Component;

/**
 * {@link DataSourceProvider}의 기본 구현체로, 애플리케이션에 등록된 {@link DataSource}로부터
 * 데이터베이스 이름을 추출하고 캐싱하여 제공하는 컴포넌트.
 *
 * <p>
 * 주요 동작:
 * <ul>
 *     <li>JDBC URL을 기반으로 MySQL, PostgreSQL, MariaDB 등의 데이터베이스 이름을 자동 추출</li>
 *     <li>H2 등 인메모리 DB인 경우 {@code "embeddedDatabase"} 반환</li>
 *     <li>인식 불가능한 경우 {@code "unknownDataSource"} 반환</li>
 *     <li>데이터베이스 이름은 최초 1회 조회 후 캐싱되어 재사용됨</li>
 * </ul>
 * </p>
 *
 * <p>
 * 이 클래스는 {@code ObjectProvider<DataSource>}를 통해 지연된 의존성 주입을 활용하므로,
 * Bean 초기화 시점에 {@code DataSource}가 없더라도 안전하게 동작한다.
 * </p>
 *
 * <pre>{@code
 * DataSourceProvider provider = new DefaultDataSourceProvider(dataSourceProvider);
 * String dbName = provider.getDataSourceName(); // 예: "mydb"
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

@Component
public class DefaultDataSourceProvider implements DataSourceProvider {

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final AtomicReference<String> cachedDatabaseName = new AtomicReference<>(null);

    public DefaultDataSourceProvider(ObjectProvider<DataSource> dataSourceProvider) {
        this.dataSourceProvider = dataSourceProvider;
    }

    /**
     * 데이터베이스 이름을 반환한다.
     * <p>
     * 내부적으로 한 번만 실제 {@link DataSource}로부터 이름을 추출하고,
     * 이후 요청부터는 캐시된 값을 반환한다.
     * </p>
     *
     * @return 데이터베이스 이름 또는 {@code unknownDataSource}
     */

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
     * 내부 {@link DataSource}로부터 커넥션을 얻어 JDBC URL을 분석하고 데이터베이스 이름을 추출한다.
     *
     * @return 추출된 데이터베이스 이름 또는 {@code unknownDataSource}
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
     * JDBC URL 문자열에서 데이터베이스 이름을 파싱하여 반환한다.
     *
     * @param url JDBC URL (예: {@code jdbc:mysql://localhost:3306/mydb})
     * @return 추출된 데이터베이스 이름 또는 {@code unknownDataSource}
     */

    private String extractDatabaseNameFromUrl(String url) {
        if (url == null) {
            return "unknownDataSource";
        }

        if (url.startsWith("jdbc:mysql://") || url.startsWith("jdbc:postgresql://") || url.startsWith("jdbc:mariadb://")) {
            String[] parts = url.split("/");
            return parts.length > 3 ? parts[3].split("\\?")[0] : "unknownDataSource";
        }

        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource instanceof EmbeddedDatabase) {
            return "embeddedDatabase";
        }

        return "unknownDataSource";
    }
}