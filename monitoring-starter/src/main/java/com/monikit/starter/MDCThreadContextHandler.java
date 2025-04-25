package com.monikit.starter;

import java.util.Map;
import java.util.concurrent.Callable;

import org.slf4j.MDC;

import com.monikit.core.concurrent.DefaultThreadContextHandler;
import com.monikit.core.LogContextScope;
import com.monikit.core.context.LogEntryContextManager;
import com.monikit.core.concurrent.ThreadContextHandler;

/**
 * SLF4J MDC 기반의 {@link ThreadContextHandler} 구현체.
 * <p>
 * - {@link LogEntryContextManager}의 컨텍스트와 함께, {@link MDC} 값을 자식 스레드로 안전하게 전파합니다.
 * - 멀티스레드 환경에서도 traceId, 로그 버퍼를 유지하며 로그 추적을 가능하게 합니다.
 * </p>
 *
 * <ul>
 *     <li>MDC 값을 {@code Map<String, String>}으로 백업하고 자식 스레드에 복원</li>
 *     <li>{@link LogContextScope}를 자동으로 함께 열어줌으로써 로그 컨텍스트 일관성 보장</li>
 *     <li>Spring Web, Executor, VirtualThread 등 모든 비동기 처리 환경에서 적용 가능</li>
 * </ul>
 *
 * <p>
 * 기본 구현인 {@link DefaultThreadContextHandler}보다 Spring SLF4J 환경에 더 적합하며,
 * {@link com.monikit.starter.config.ThreadContextHandlerAutoConfiguration}을 통해 자동 등록됩니다.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.1.2
 */

public class MDCThreadContextHandler extends DefaultThreadContextHandler {

    private final LogEntryContextManager logEntryContextManager;

    public MDCThreadContextHandler(LogEntryContextManager logEntryContextManager) {
        this.logEntryContextManager = logEntryContextManager;
    }

    @Override
    public Runnable propagateToChildThread(Runnable task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    task.run();
                } finally {
                    MDC.clear();
                }
            }
        };
    }

    @Override
    public <T> Callable<T> propagateToChildThread(Callable<T> task) {
        Map<String, String> contextMap = MDC.getCopyOfContextMap();

        return () -> {
            try (LogContextScope scope = new LogContextScope(logEntryContextManager)) {
                if (contextMap != null) {
                    MDC.setContextMap(contextMap);
                }
                try {
                    return task.call();
                } finally {
                    MDC.clear();
                }
            }
        };
    }

}
