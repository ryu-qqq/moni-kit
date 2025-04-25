package com.monikit.core.notifier;

import com.monikit.core.LogLevel;
import com.monikit.core.model.LogEntry;

/**
 * 로그를 출력하는 인터페이스.
 *
 * @author ryu-qqq
 * @since 1.0.0
 */

public interface LogNotifier {
    void notify(LogLevel logLevel, String message);
    void notify(LogEntry logEntry);
}
