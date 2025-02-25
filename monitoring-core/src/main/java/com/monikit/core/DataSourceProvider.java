package com.monikit.core;

/**
 * DataSource 이름을 제공하는 인터페이스.
 * <p>
 * `monitoring-starter`에서 구현체를 제공해야 하며, Spring 빈으로 관리됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */
public interface DataSourceProvider {

    /**
     * 현재 사용 중인 데이터소스 이름을 반환한다.
     *
     * @return 데이터소스 이름 (없으면 "unknownDataSource")
     */
    String getDataSourceName();
}