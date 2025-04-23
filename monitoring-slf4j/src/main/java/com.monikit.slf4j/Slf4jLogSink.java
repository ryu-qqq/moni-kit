package com.monikit.slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.monikit.core.LogEntry;
import com.monikit.core.LogSink;
import com.monikit.core.LogType;


/**
 * SLF4J 기반 기본 {@link LogSink} 구현체.
 * <p>
 * {@link LogEntry}의 내용을 SLF4J Logger를 통해 로그로 출력합니다.
 * </p>
 *
 * <p>
 * 로그 레벨은 {@link LogEntry#getLogLevel()}에 따라 SLF4J의
 * {@code info}, {@code warn}, {@code error}, {@code debug}로 자동 분기됩니다.
 * </p>
 *
 *
 * @author ryu-qqq
 * @since 1.1.2
 */
public class Slf4jLogSink implements LogSink {

    private static final Logger logger = LoggerFactory.getLogger(Slf4jLogSink.class);

    /**
     * 지원 여부 확인. {@link Slf4jLogSink}는 모든 {@link LogType}을 지원합니다.
     *
     * @param logType 로그 타입
     * @return 항상 {@code true}
     */

    @Override
    public boolean supports(LogType logType) {
        return true;
    }

    /**
     * 로그를 SLF4J를 통해 출력합니다.
     *
     * @param logEntry 출력할 로그
     */

    @Override
    public void send(LogEntry logEntry) {
        switch (logEntry.getLogLevel()) {
            case INFO -> logger.info(logEntry.toString());
            case WARN -> logger.warn(logEntry.toString());
            case ERROR -> logger.error(logEntry.toString());
            default -> logger.debug(logEntry.toString());
        }
    }

}
