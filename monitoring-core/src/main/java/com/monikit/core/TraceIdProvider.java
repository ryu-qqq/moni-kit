package com.monikit.core;

/**
 * Trace ID를 제공하는 인터페이스 (static 기반).
 * <p>
 * - 기본적으로 `NoOpTraceIdProvider`를 사용하지만, 사용자가 직접 설정 가능.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public interface TraceIdProvider {
    String getTraceId();

    /**
     * 현재 설정된 TraceIdProvider 인스턴스
     */
    TraceIdProvider INSTANCE = new NoOpTraceIdProvider();

    /**
     * TraceIdProvider의 구현체를 설정하는 메서드.
     *
     * @param provider 사용자 정의 TraceIdProvider
     */
    static void setInstance(TraceIdProvider provider) {
        if (provider != null) {
            INSTANCE_HOLDER.provider = provider;
        }
    }

    /**
     * 현재 Trace ID를 반환하는 정적 메서드.
     *
     * @return 현재 Trace ID
     */
    static String currentTraceId() {
        return INSTANCE_HOLDER.provider.getTraceId();
    }


    /**
     * TraceIdProvider 인스턴스를 저장하는 내부 정적 클래스 (Lazy Initialization)
     */
    class INSTANCE_HOLDER {
        private static TraceIdProvider provider = new NoOpTraceIdProvider();
    }
}

/**
 * 기본 `TraceIdProvider` 구현 (Trace ID가 없을 경우 "N/A" 반환).
 */
class NoOpTraceIdProvider implements TraceIdProvider {
    @Override
    public String getTraceId() {
        return "N/A";
    }
}
