package com.monikit.core;

/**
 * DataSource 이름을 제공하는 인터페이스.
 * <p>
 * `monitoring-starter`에서 구현체를 제공해야 함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public interface DataSourceProvider {
    /**
     * 현재 사용 중인 데이터소스 이름을 반환한다.
     *
     * @return 데이터소스 이름 (없으면 "unknownDataSource")
     */
    String getDataSourceName();

    /**
     * 기본적으로 "unknownDataSource"를 반환하는 기본 구현체.
     */
    DataSourceProvider INSTANCE = new DefaultDataSourceProvider();

    /**
     * 외부에서 `DataSourceProvider`의 구현체를 주입할 수 있도록 한다.
     *
     * @param provider 사용자 정의 `DataSourceProvider` 구현체
     */
    static void setInstance(DataSourceProvider provider) {
        if (provider != null) {
            INSTANCE_HOLDER.provider = provider;
        }
    }

    /**
     * 현재 설정된 DataSourceProvider 인스턴스를 반환.
     */
    static String currentDataSourceName() {
        return INSTANCE_HOLDER.provider.getDataSourceName();
    }

    /**
     * 내부 정적 클래스 (Lazy Initialization)
     */
    class INSTANCE_HOLDER {
        private static DataSourceProvider provider = new DefaultDataSourceProvider();
    }
}

/**
 * 기본 `DataSourceProvider` 구현 (데이터소스를 알 수 없는 경우 사용).
 */
class DefaultDataSourceProvider implements DataSourceProvider {
    @Override
    public String getDataSourceName() {
        return "unknownDataSource";
    }
}
