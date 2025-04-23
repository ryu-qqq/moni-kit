package com.monikit.starter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ArgumentUtils 테스트")
class ArgumentUtilsTest {

    @Nested
    @DisplayName("safeArgsToString() 테스트")
    class SafeArgsToStringTests {

        @Test
        @DisplayName("여러 타입의 인자를 문자열로 변환해야 한다.")
        void shouldConvertVariousArgsToStringSafely() {
            Object[] args = { "abc", 123, true };
            String result = ArgumentUtils.safeArgsToString(args);

            assertEquals("[arg0=abc, arg1=123, arg2=true]", result);
        }

        @Test
        @DisplayName("null 인자를 안전하게 처리해야 한다.")
        void shouldHandleNullArgs() {
            Object[] args = { null };
            String result = ArgumentUtils.safeArgsToString(args);

            assertEquals("[arg0=null]", result);
        }

        @Test
        @DisplayName("toString() 호출 시 예외가 발생해도 [unserializable]로 처리해야 한다.")
        void shouldHandleUnserializableArgsGracefully() {
            Object[] args = { new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("fail");
                }
            }};

            String result = ArgumentUtils.safeArgsToString(args);
            assertEquals("[arg0=[unserializable]]", result);
        }
    }

    @Nested
    @DisplayName("safeOutputToString() 테스트")
    class SafeOutputToStringTests {

        @Test
        @DisplayName("null 반환값을 'null'로 표현해야 한다.")
        void shouldReturnNullStringIfOutputIsNull() {
            String result = ArgumentUtils.safeOutputToString(null);
            assertEquals("null", result);
        }

        @Test
        @DisplayName("정상적인 객체는 toString() 결과로 반환해야 한다.")
        void shouldReturnToStringIfSerializable() {
            String result = ArgumentUtils.safeOutputToString("hello");
            assertEquals("hello", result);
        }

        @Test
        @DisplayName("toString()이 실패하면 [unserializable]로 반환해야 한다.")
        void shouldReturnUnserializableIfToStringFails() {
            Object obj = new Object() {
                @Override
                public String toString() {
                    throw new RuntimeException("fail");
                }
            };

            String result = ArgumentUtils.safeOutputToString(obj);
            assertEquals("[unserializable]", result);
        }
    }
}