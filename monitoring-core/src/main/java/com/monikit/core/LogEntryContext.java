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
     * 현재 요청의 로그를 모두 제거한다.
     * (요청이 끝나면 호출해야 함)
     */
    static void clear() {
        logThreadLocal.remove();
    }

    /**
     * 현재 스레드의 컨텍스트를 자식 스레드로 전달하는 Runnable을 생성한다.
     *
     * @param task 실행할 Runnable
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 Runnable
     */
    static Runnable propagateToChildThread(Runnable task) {
        Queue<LogEntry> parentLogs = new ConcurrentLinkedQueue<>(logThreadLocal.get());

        return () -> {
            logThreadLocal.set(new ConcurrentLinkedQueue<>(parentLogs));
            task.run();
        };
    }


}
