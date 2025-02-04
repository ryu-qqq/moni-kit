package com.monikit.starter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.monikit.core.LogEntry;

/**
 * Utility class to convert LogEntry objects to JSON.
 * <p>
 * This class is stateless and provides a static method to convert logs into JSON format.
 * </p>
 *
 * @author ryu-qqq
 * @since 1.0
 */
public class LogEntryJsonConverter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


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
            return OBJECT_MAPPER.writeValueAsString(logEntry);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize LogEntry to JSON", e);
        }
    }

}
