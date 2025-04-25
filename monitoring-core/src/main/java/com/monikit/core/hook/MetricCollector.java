package com.monikit.core.hook;

import com.monikit.core.LogType;
import com.monikit.core.model.LogEntry;

/**
 * 모든 메트릭 수집기를 위한 공통 인터페이스.
 * <p>
 * - LogType 에 따라 다양한 메트릭 시스템과 연동할 수 있도록 설계.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.1
 */
public interface MetricCollector <T extends LogEntry>{

    boolean supports(LogType logType);
    void record(T logEntry);
}
