package com.monikit.core;

/**
 * Trace ID를 제공하는 인터페이스 (static 기반).
 * <p>
 * - 기본적으로 `NoOpTraceIdProvider`를 사용하지만, 사용자가 직접 설정 가능.
 * - 멀티 스레드 환경에서도 안전하게 동작하도록 보장됨.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public interface TraceIdProvider {
    static String currentTraceId() {
        return InstanceHolder.INSTANCE.getTraceId();
    }

    static void setInstance(TraceIdProvider provider) {
        if (provider != null) {
            InstanceHolder.setInstance(provider);
        }
    }

    String getTraceId();

    class InstanceHolder {
        private static volatile TraceIdProvider INSTANCE;

        static {
            INSTANCE = new NoOpTraceIdProvider(); // ✅ 명확하게 정적 초기화
        }

        static TraceIdProvider getInstance() {
            return INSTANCE;
        }

        static synchronized void setInstance(TraceIdProvider provider) {
            if (provider != null) {
                INSTANCE = provider;
            }
        }
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
