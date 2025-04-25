package com.monikit.jdbc;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DefaultDataSourceProvider 테스트")
class DefaultDataSourceProviderTest {

    private final ObjectProvider<DataSource> mockDataSourceProvider = mock(ObjectProvider.class);
    private final DefaultDataSourceProvider dataSourceProvider = new DefaultDataSourceProvider(mockDataSourceProvider);

    @Nested
    @DisplayName("데이터베이스 이름 추출 테스트")
    class DataSourceNameExtractionTests {

        @Test
        @DisplayName("MySQL 데이터소스에서 올바르게 데이터베이스 이름을 추출해야 한다.")
        void shouldExtractDatabaseNameFromMySQL() throws SQLException {
            // Given
            String jdbcUrl = "jdbc:mysql://localhost:3306/testdb?useSSL=false";
            mockDatabaseMetadata(jdbcUrl);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("testdb", dbName);
        }

        @Test
        @DisplayName("PostgreSQL 데이터소스에서 올바르게 데이터베이스 이름을 추출해야 한다.")
        void shouldExtractDatabaseNameFromPostgreSQL() throws SQLException {
            // Given
            String jdbcUrl = "jdbc:postgresql://localhost:5432/sampledb";
            mockDatabaseMetadata(jdbcUrl);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("sampledb", dbName);
        }

        @Test
        @DisplayName("MariaDB 데이터소스에서 올바르게 데이터베이스 이름을 추출해야 한다.")
        void shouldExtractDatabaseNameFromMariaDB() throws SQLException {
            // Given
            String jdbcUrl = "jdbc:mariadb://localhost:3307/mydb";
            mockDatabaseMetadata(jdbcUrl);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("mydb", dbName);
        }

        @Test
        @DisplayName("H2 또는 인메모리 DB는 'embeddedDatabase'를 반환해야 한다.")
        void shouldReturnEmbeddedDatabaseForH2() throws SQLException {
            // Given
            EmbeddedDatabase embeddedDatabase = Mockito.mock(EmbeddedDatabase.class);
            Connection mockConnection = mock(Connection.class);
            DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);

            when(embeddedDatabase.getConnection()).thenReturn(mockConnection);
            when(mockConnection.getMetaData()).thenReturn(mockMetaData);
            when(mockMetaData.getURL()).thenReturn("jdbc:h2:mem:testdb");

            when(mockDataSourceProvider.getIfAvailable()).thenReturn(embeddedDatabase);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("embeddedDatabase", dbName);
        }


        @Test
        @DisplayName("데이터소스를 감지할 수 없으면 'unknownDataSource'를 반환해야 한다.")
        void shouldReturnUnknownDataSourceWhenUnavailable() {
            // Given
            when(mockDataSourceProvider.getIfAvailable()).thenReturn(null);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("unknownDataSource", dbName);
        }

        @Test
        @DisplayName("JDBC URL이 null이면 'unknownDataSource'를 반환해야 한다.")
        void shouldReturnUnknownDataSourceForNullUrl() throws SQLException {
            // Given
            mockDatabaseMetadata(null);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("unknownDataSource", dbName);
        }

        @Test
        @DisplayName("SQLException 발생 시 'unknownDataSource'를 반환해야 한다.")
        void shouldReturnUnknownDataSourceWhenSQLExceptionOccurs() throws SQLException {
            // Given
            DataSource mockDataSource = mock(DataSource.class);
            when(mockDataSource.getConnection()).thenThrow(new SQLException("Connection error"));
            when(mockDataSourceProvider.getIfAvailable()).thenReturn(mockDataSource);

            // When
            String dbName = dataSourceProvider.getDataSourceName();

            // Then
            assertEquals("unknownDataSource", dbName);
        }
    }

    /**
     * Mock을 이용하여 데이터베이스 메타데이터를 설정하는 유틸리티 메서드.
     */
    private void mockDatabaseMetadata(String jdbcUrl) throws SQLException {
        DataSource mockDataSource = mock(DataSource.class);
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetaData = mock(DatabaseMetaData.class);

        when(mockDataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.getMetaData()).thenReturn(mockMetaData);
        when(mockMetaData.getURL()).thenReturn(jdbcUrl);
        when(mockDataSourceProvider.getIfAvailable()).thenReturn(mockDataSource);

    }

}