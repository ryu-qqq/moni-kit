package com.monikit.core;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 요청 단위로 로그를 저장하고 관리하는 컨텍스트.
 * <p>
 * InheritableThreadLocal을 사용하여 부모 스레드에서 자식 스레드로 로그 컨텍스트를 전파할 수 있도록 한다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
class LogEntryContext {

    private static final InheritableThreadLocal<Queue<LogEntry>> logThreadLocal =
        new InheritableThreadLocal<>() {
            @Override
            protected Queue<LogEntry> initialValue() {
                return new ConcurrentLinkedQueue<>();
            }
        };

    private static final InheritableThreadLocal<Boolean> hasError =
        new InheritableThreadLocal<>() {
            @Override
            protected Boolean initialValue() {
                return false;
            }
        };


    /**
     * 현재 요청(스레드)에서 실행된 로그를 저장한다.
     *
     * @param logEntry 저장할 로그 객체
     */
    static void addLog(LogEntry logEntry) {
        logThreadLocal.get().add(logEntry);
    }

    /**
     * 현재 요청(스레드)에서 실행된 모든 로그를 반환한다.
     *
     * @return 현재 요청에서 발생한 로그 리스트
     */
    static Queue<LogEntry> getLogs() {
        return new ConcurrentLinkedQueue<>(logThreadLocal.get());
    }

    /**
     *
     * @return 현재 요청에서 발생한 로그 리스트의 사이즈
     */
    static int size() {
        return logThreadLocal.get().size();
    }

    /**
     * 현재 요청의 로그를 모두 제거한다.
     * (요청이 끝나면 호출해야 함)
     */
    static void clear() {
        logThreadLocal.remove();
        hasError.set(false);
    }

    /**
     * 현재 요청에서 예외가 발생했는지 여부를 반환한다.
     *
     * @return 예외 발생 여부 (true면 예외 발생)
     */
    static boolean hasError() {
        return hasError.get();
    }

    /**
     * 현재 요청에서 예외가 발생했음을 설정한다.
     */
    static void setErrorOccurred(boolean errorOccurred) {
        hasError.set(errorOccurred);
    }

}
