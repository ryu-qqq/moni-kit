package com.monikit.starter;


import com.monikit.core.LogEntry;

/**
 * Utility class to convert LogEntry objects to JSON.
 * <p>
 * This class is stateless and provides a static method to convert logs into JSON format.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0.0
 */

@Deprecated
public class LogEntryJsonConverter {



    private LogEntryJsonConverter() {
    }

    /**
     * Converts a LogEntry object to JSON string.
     *
     * @param logEntry LogEntry instance
     * @return JSON formatted string
     * @throws RuntimeException if JSON serialization fails
     */
    public static String toJson(LogEntry logEntry) {
        try {
            return logEntry.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize LogEntry to JSON", e);
        }
    }



}
