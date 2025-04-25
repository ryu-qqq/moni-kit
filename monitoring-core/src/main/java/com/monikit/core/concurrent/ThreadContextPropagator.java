package com.monikit.core.concurrent;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.monikit.core.context.LogEntryContext;
import com.monikit.core.model.LogEntry;

/**
 * 스레드 컨텍스트 전파를 담당하는 클래스.
 * <p>
 * - 부모 스레드의 로그 컨텍스트를 자식 스레드로 복사하여 실행할 수 있도록 지원한다.
 * - {@link LogEntryContext}에서 로그 데이터를 가져와 복사함.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */
public class ThreadContextPropagator {

    /**
     * 부모 스레드의 컨텍스트를 자식 스레드로 전달하는 Runnable을 생성한다.
     *
     * @param task 실행할 Runnable
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 Runnable
     */
    public static Runnable propagateToChildThread(Runnable task) {
        Queue<LogEntry> parentLogs = new ConcurrentLinkedQueue<>(LogEntryContext.getLogs());
        boolean parentHasError = LogEntryContext.hasError();

        return () -> {
            LogEntryContext.clear();
            parentLogs.forEach(LogEntryContext::addLog);
            LogEntryContext.setErrorOccurred(parentHasError);
            task.run();
        };
    }

    /**
     * 부모 스레드의 컨텍스트를 자식 스레드로 전달하는 Callable을 생성한다.
     *
     * @param <T> 반환 타입
     * @param task 실행할 Callable
     * @return 부모 스레드의 컨텍스트가 복사된 새로운 Callable
     */
    public static <T> Callable<T> propagateToChildThread(Callable<T> task) {
        Queue<LogEntry> parentLogs = new ConcurrentLinkedQueue<>(LogEntryContext.getLogs());
        boolean parentHasError = LogEntryContext.hasError();

        return () -> {
            LogEntryContext.clear();
            parentLogs.forEach(LogEntryContext::addLog);
            LogEntryContext.setErrorOccurred(parentHasError);
            return task.call();
        };
    }

}