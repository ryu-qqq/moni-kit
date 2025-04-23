package com.monikit.starter;

import java.util.List;
import java.util.Optional;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.monikit.config.DynamicLogRule;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("DynamicMatcher 테스트")
class DynamicMatcherTest {

    @Nested
    @DisplayName("findMatchingRule() 동작 테스트")
    class FindMatchingRuleTests {

        @Test
        @DisplayName("클래스, 메서드 이름이 규칙과 일치하고 when이 null이면 매칭되어야 한다.")
        void shouldMatchRuleWithoutWhenExpression() {
            // Given
            DynamicLogRule rule = new DynamicLogRule();
            rule.setClassNamePattern("TestService");
            rule.setMethodNamePattern("runTask");
            rule.setWhen(null);  // 조건식 없음

            DynamicMatcher matcher = new DynamicMatcher(List.of(rule), List.of());

            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            MethodSignature signature = mock(MethodSignature.class);
            when(joinPoint.getTarget()).thenReturn(new TestService());
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(TestService.class.getDeclaredMethods()[0]);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});

            // When
            Optional<DynamicLogRule> matched = matcher.findMatchingRule(joinPoint, 300);

            // Then
            assertTrue(matched.isPresent(), "규칙이 매칭되어야 한다.");
        }

        @Test
        @DisplayName("클래스가 허용되지 않은 패키지이면 매칭되지 않아야 한다.")
        void shouldReturnEmptyIfClassNotInAllowedPackage() throws NoSuchMethodException {
            // given
            DynamicLogRule rule = new DynamicLogRule();
            rule.setClassNamePattern("UnmatchedService");
            rule.setMethodNamePattern("anyMethod");

            DynamicMatcher matcher = new DynamicMatcher(List.of(rule), List.of("com.forbidden")); // ❗ 일부러 걸리게 함

            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            MethodSignature signature = mock(MethodSignature.class);
            when(joinPoint.getTarget()).thenReturn(new UnmatchedService());
            when(joinPoint.getSignature()).thenReturn(signature); // ✅ 꼭 필요!
            when(signature.getMethod()).thenReturn(UnmatchedService.class.getDeclaredMethod("anyMethod"));
            when(joinPoint.getArgs()).thenReturn(new Object[]{});

            // when
            Optional<DynamicLogRule> matched = matcher.findMatchingRule(joinPoint, 100);

            // then
            assertTrue(matched.isEmpty(), "패키지가 허용되지 않으면 매칭되지 않아야 한다.");
        }

        @Test
        @DisplayName("SpEL 조건식이 true이면 매칭되어야 한다.")
        void shouldMatchRuleWhenSpELConditionIsTrue() {
            DynamicLogRule rule = new DynamicLogRule();
            rule.setClassNamePattern("TestService");
            rule.setMethodNamePattern("runTask");
            rule.setWhen("#executionTime > 200");

            DynamicMatcher matcher = new DynamicMatcher(List.of(rule), List.of());

            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            MethodSignature signature = mock(MethodSignature.class);
            when(joinPoint.getTarget()).thenReturn(new TestService());
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(TestService.class.getDeclaredMethods()[0]);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});

            Optional<DynamicLogRule> matched = matcher.findMatchingRule(joinPoint, 300);

            assertTrue(matched.isPresent(), "SpEL 조건이 true면 매칭되어야 한다.");
        }

        @Test
        @DisplayName("SpEL 조건식이 false이면 매칭되지 않아야 한다.")
        void shouldNotMatchRuleWhenSpELConditionIsFalse() {
            DynamicLogRule rule = new DynamicLogRule();
            rule.setClassNamePattern("TestService");
            rule.setMethodNamePattern("runTask");
            rule.setWhen("#executionTime < 100");

            DynamicMatcher matcher = new DynamicMatcher(List.of(rule), List.of("com.example"));

            ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
            MethodSignature signature = mock(MethodSignature.class);
            when(joinPoint.getTarget()).thenReturn(new TestService());
            when(joinPoint.getSignature()).thenReturn(signature);
            when(signature.getMethod()).thenReturn(TestService.class.getDeclaredMethods()[0]);
            when(joinPoint.getArgs()).thenReturn(new Object[]{});

            Optional<DynamicLogRule> matched = matcher.findMatchingRule(joinPoint, 300);

            assertTrue(matched.isEmpty(), "SpEL 조건이 false면 매칭되지 않아야 한다.");
        }
    }

    // 테스트용 클래스
    static class TestService {
        public void runTask() {}
    }

    static class UnmatchedService {
        public void anyMethod() {}
    }
}