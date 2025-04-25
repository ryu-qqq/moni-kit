package com.monikit.starter.batch;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;

import com.monikit.core.LogLevel;

public final class BatchLogLevelResolver {

    private BatchLogLevelResolver() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static LogLevel resolveFromJob(BatchStatus status, ExitStatus exitStatus) {
        if (status == BatchStatus.FAILED || status == BatchStatus.STOPPED) {
            return LogLevel.ERROR;
        }

        if (status == BatchStatus.UNKNOWN || status == BatchStatus.ABANDONED) {
            return LogLevel.WARN;
        }

        return resolveFromExitCode(exitStatus.getExitCode());
    }


    public static LogLevel resolveLogStep(ExitStatus exitStatus) {
        String code = exitStatus.getExitCode();
        if (code.contains("FAILURE") || code.contains("EXCEPTION") || code.contains("ERROR") || code.contains("TIMEOUT")) {
            return LogLevel.ERROR;
        }
        if (code.contains("WARNING") || code.contains("SKIP")) {
            return LogLevel.WARN;
        }
        return LogLevel.INFO;
    }

    public static LogLevel resolveFromExitCode(String exitCode) {
        if (exitCode == null) return LogLevel.INFO;

        String upper = exitCode.toUpperCase();
        if (upper.contains("FAILURE") || upper.contains("EXCEPTION") || upper.contains("ERROR") || upper.contains("TIMEOUT")) {
            return LogLevel.ERROR;
        }
        if (upper.contains("WARNING") || upper.contains("SKIP")) {
            return LogLevel.WARN;
        }
        return LogLevel.INFO;
    }
}
