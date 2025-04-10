package com.monikit.core;

import java.util.concurrent.Callable;

/**
 * 기본 스레드 컨텍스트 전파 구현체.
 * <p>
 * - 부모 스레드의 로그 컨텍스트를 자식 스레드로 전달하는 기능을 제공한다.
 * - logException 의 코드에서 {@link ErrorCategory} 를 제거했다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.0
 */

public class DefaultThreadContextHandler implements ThreadContextHandler {

    @Override
    public Runnable propagateToChildThread(Runnable task) {
        return ThreadContextPropagator.propagateToChildThread(task);
    }

    @Override
    public <T> Callable<T> propagateToChildThread(Callable<T> task) {
        return ThreadContextPropagator.propagateToChildThread(task);
    }

    @Override
    public <T> ThrowingCallable<T> propagateToChildThreadThrowable(ThrowingCallable<T> task) {
        return () -> {
            Callable<T> callable = propagateToChildThread(() -> {
                try {
                    return task.call();
                } catch (Throwable throwable) {
                    throw propagateAsException(throwable);
                }
            });
            return callable.call();
        };
    }

    private static Exception propagateAsException(Throwable throwable) {
        if (throwable instanceof Exception) {
            return (Exception) throwable;
        }
        return new RuntimeException(throwable);
    }

    @Override
    public void logException(String traceId, Throwable exception) {
        if (LogEntryContext.hasError()) {
            return;
        }
        LogEntryContext.addLog(ExceptionLog.create(traceId, exception));
        LogEntryContext.setErrorOccurred(true);
    }

    @Override
    public void propagateAndRun(Runnable task) {
        propagateToChildThread(task).run();
    }

    @Override
    public <T> T propagateAndCall(Callable<T> task) throws Exception {
        return propagateToChildThread(task).call();
    }

    @Override
    public <T> T propagateAndCallThrowable(ThrowingCallable<T> task) throws Throwable {
        return propagateToChildThreadThrowable(task).call();
    }

}
