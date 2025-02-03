package com.monikit.starter;

import java.util.concurrent.Callable;

import com.monikit.core.LogEntryContext;
import com.monikit.core.LogEntryContextManager;

/**
 * 스레드 컨텍스트를 유지 및 전파하는 유틸리티 클래스.
 * <p>
 * - 새로운 스레드가 생성될 경우, 부모 스레드의 컨텍스트를 자동으로 복사함.
 * - 실행할 로직을 감싸서 실행하는 기능을 제공.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class ThreadContextPropagator {

    /**
     * 주어진 ThrowingCallable을 실행하면서, 필요한 경우 스레드 컨텍스트를 복사하여 유지한다.
     *
     * @param <T> 반환 타입
     * @param task 실행할 Callable
     * @return 실행 결과
     * @throws Exception 예외 발생 가능
     */
    public static <T> T runWithContext(ThrowingCallable<T> task) throws Exception {
        if (LogEntryContext.getLogs().isEmpty()) {
            Callable<T> wrappedTask = LogEntryContextManager.propagateToChildThread(() -> {
                try {
                    return task.call();
                } catch (Throwable t) {
                    throw propagateAsException(t);
                }
            });
            return wrappedTask.call();
        } else {
            try {
                return task.call();
            } catch (Throwable t) {
                throw propagateAsException(t);
            }
        }
    }

    /**
     * Throwable을 Exception으로 변환
     *
     * @param throwable 발생한 Throwable
     * @return 변환된 Exception
     */
    private static Exception propagateAsException(Throwable throwable) {
        if (throwable instanceof Exception) {
            return (Exception) throwable;
        }
        return new RuntimeException(throwable);
    }

    @FunctionalInterface
    public interface ThrowingCallable<T> {
        T call() throws Throwable;
    }

}