package com.monikit.jdbc;

/**
 * {@link javax.sql.DataSource}의 식별 이름을 제공하는 전략 인터페이스.
 *
 * <p>
 * 이 인터페이스는 SQL 실행 로그 또는 메트릭에서 데이터소스를 구분하기 위한 이름을 제공한다.
 * 주로 {@code QueryLoggingService}에서 사용되며, 모니터링 대상 DB를 명확하게 식별하는 데에 활용된다.
 * </p>
 *
 * <p>
 * {@code monitoring-starter-jdbc}에서는 기본 구현체로 {@link DefaultDataSourceProvider}를 제공하며,
 * 사용자 정의 데이터소스 이름 전략이 필요한 경우 이 인터페이스를 구현한 커스텀 빈을 등록하면 된다.
 * </p>
 *
 * <pre>{@code
 * @Component
 * public class MyDataSourceProvider implements DataSourceProvider {
 *     @Override
 *     public String getDataSourceName() {
 *         return "my-main-db";
 *     }
 * }
 * }</pre>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public interface DataSourceProvider {

    /**
     * 현재 사용 중인 데이터소스의 이름을 반환한다.
     *
     * <p>
     * 기본적으로는 JDBC URL을 파싱하거나, H2 등 인메모리 DB의 경우 고정 문자열을 반환한다.
     * </p>
     *
     * @return 데이터소스 이름 (식별 불가능한 경우 {@code "unknownDataSource"})
     */

    String getDataSourceName();
}