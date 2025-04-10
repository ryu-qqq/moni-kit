package com.monikit.core;

import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.monikit.core.utils.TestLogEntryProvider;

import static java.util.concurrent.Executors.newSingleThreadExecutor;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ThreadContextPropagator 테스트")
class ThreadContextPropagatorTest {

    @BeforeEach
    void setup() {
        LogEntryContext.clear();
        LogEntryContext.setErrorOccurred(false);
    }

    @Nested
    @DisplayName("Runnable 기반 컨텍스트 전파 테스트")
    class RunnablePropagationTests {

        @Test
        @DisplayName("should propagate log context to child thread using Runnable")
        void shouldPropagateLogContextToChildThreadUsingRunnable() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Runnable childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
                assertTrue(LogEntryContext.hasError());
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();
        }

        @Test
        @DisplayName("should clear child thread context without affecting parent")
        void shouldClearChildThreadContextWithoutAffectingParent() throws InterruptedException {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Runnable childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                LogEntryContext.clear();
                assertEquals(0, LogEntryContext.size());
                assertFalse(LogEntryContext.hasError());
            });

            Thread thread = new Thread(childTask);
            thread.start();
            thread.join();

            // 부모 스레드의 컨텍스트는 유지되어야 함
            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.hasError());
        }
    }

    @Nested
    @DisplayName("Callable 기반 컨텍스트 전파 테스트")
    class CallablePropagationTests {

        @Test
        @DisplayName("should propagate log context to child thread using Callable and return expected result")
        void shouldPropagateLogContextToChildThreadUsingCallableAndReturnExpectedResult() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                Queue<LogEntry> logs = LogEntryContext.getLogs();
                assertEquals(1, logs.size());
                assertTrue(logs.contains(log));
                assertTrue(LogEntryContext.hasError());
                return true;
            });

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean result = future.get();
            executor.shutdown();

            assertTrue(result);
        }

        @Test
        @DisplayName("should propagate error state to child thread using Callable")
        void shouldPropagateErrorStateToChildThreadUsingCallable() throws Exception {
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(LogEntryContext::hasError);

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean hasErrorInChildThread = future.get();
            executor.shutdown();

            assertTrue(hasErrorInChildThread);
        }

        @Test
        @DisplayName("should clear child thread context without affecting parent using Callable")
        void shouldClearChildThreadContextWithoutAffectingParentUsingCallable() throws Exception {
            LogEntry log = TestLogEntryProvider.executionTimeLog();
            LogEntryContext.addLog(log);
            LogEntryContext.setErrorOccurred(true);

            Callable<Boolean> childTask = ThreadContextPropagator.propagateToChildThread(() -> {
                LogEntryContext.clear();
                assertEquals(0, LogEntryContext.size());
                assertFalse(LogEntryContext.hasError());
                return true;
            });

            ExecutorService executor = newSingleThreadExecutor();
            Future<Boolean> future = executor.submit(childTask);
            boolean result = future.get();
            executor.shutdown();

            assertTrue(result);

            // 부모 스레드의 컨텍스트는 유지되어야 함
            assertEquals(1, LogEntryContext.size());
            assertTrue(LogEntryContext.hasError());
        }
    }

    @Nested
    @DisplayName("ConcurrentLinkedQueue 레이스 컨딕션 테스트")
    class ConcurrentLinkedQueueRaceTest {
        @Test
        void testRaceConditionOnConcurrentLinkedQueue() throws InterruptedException {
            Queue<Integer> queue = new ConcurrentLinkedQueue<>();
            AtomicInteger pollCount = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(1);
            int itemCount = 10000;

            for (int i = 0; i < itemCount; i++) {
                queue.offer(i);
            }

            Runnable poller = () -> {
                try {
                    latch.await();
                    for (int i = 0; i < itemCount / 2; i++) {
                        // 비원자적 복합 연산
                        if (!queue.isEmpty()) {
                            Integer item = queue.poll();
                            if (item != null) {
                                pollCount.incrementAndGet();
                            }
                        }
                    }
                } catch (InterruptedException ignored) {
                }
            };

            Thread t1 = new Thread(poller);
            Thread t2 = new Thread(poller);

            t1.start();
            t2.start();

            latch.countDown();

            t1.join();
            t2.join();

            System.out.println("Expected <= " + itemCount + ", Actual: " + pollCount.get());

            assertTrue(pollCount.get() <= itemCount);
        }
    }

    // @Nested
    // @DisplayName("ConcurrentLinkedQueue Race Condition Stress Test")
    // class ConcurrentLinkedQueueRaceStressTest {
    //
    //     @RepeatedTest(50)
    //     @DisplayName("복합 연산의 경쟁 조건이 실제로 발생하는지 반복 테스트")
    //     void shouldTriggerRaceConditionWithStress() throws InterruptedException {
    //         Queue<Integer> queue = new ConcurrentLinkedQueue<>();
    //         AtomicInteger pollCount = new AtomicInteger(0);
    //         CountDownLatch latch = new CountDownLatch(1);
    //         int itemCount = 100_000;
    //         int threadCount = 16;
    //
    //         for (int i = 0; i < itemCount; i++) {
    //             queue.offer(i);
    //         }
    //
    //         Runnable poller = () -> {
    //             try {
    //                 latch.await();
    //                 for (int i = 0; i < itemCount / threadCount; i++) {
    //                     if (!queue.isEmpty()) {
    //                         Thread.sleep(1);
    //                         Integer item = queue.poll();
    //                         if (item != null) {
    //                             pollCount.incrementAndGet();
    //                         }
    //                     }
    //                 }
    //             } catch (InterruptedException ignored) {
    //             }
    //         };
    //
    //         Thread[] threads = new Thread[threadCount];
    //         for (int i = 0; i < threadCount; i++) {
    //             threads[i] = new Thread(poller);
    //             threads[i].start();
    //         }
    //
    //         latch.countDown();
    //
    //         for (Thread t : threads) {
    //             t.join();
    //         }
    //
    //         int result = pollCount.get();
    //         if (result < itemCount) {
    //             System.out.println("Race condition 재현됨! Expected: " + itemCount + ", Actual: " + result);
    //         } else {
    //             System.out.println("정상 처리: " + result);
    //         }
    //
    //         assertTrue(result <= itemCount);
    //     }
    // }


}