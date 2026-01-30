package com.smartchain.platform.domain.ai.validation;

import com.smartchain.platform.global.enums.AiVerdict;
import com.smartchain.platform.global.enums.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AI 응답 값 검증 테스트")
class AiResponseValidationTest {

    @Nested
    @DisplayName("AiVerdict 검증")
    class AiVerdictValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {"PASS", "WARN", "NEED_CLARIFY", "NEED_FIX"})
        @DisplayName("유효한 verdict 값은 true를 반환해야 한다")
        void shouldReturnTrueForValidVerdict(String value) {
            assertThat(AiVerdict.isValid(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"pass", "warn", "need_clarify", "need_fix"})
        @DisplayName("소문자 verdict 값도 유효해야 한다")
        void shouldReturnTrueForLowercaseVerdict(String value) {
            assertThat(AiVerdict.isValid(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"FAIL", "PENDING", "INVALID", "SUCCESS", "ERROR", ""})
        @DisplayName("유효하지 않은 verdict 값은 false를 반환해야 한다")
        void shouldReturnFalseForInvalidVerdict(String value) {
            assertThat(AiVerdict.isValid(value)).isFalse();
        }

        @Test
        @DisplayName("null verdict 값은 false를 반환해야 한다")
        void shouldReturnFalseForNullVerdict() {
            assertThat(AiVerdict.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("fromString은 유효한 값을 enum으로 변환해야 한다")
        void shouldConvertValidStringToEnum() {
            assertThat(AiVerdict.fromString("PASS")).isEqualTo(AiVerdict.PASS);
            assertThat(AiVerdict.fromString("warn")).isEqualTo(AiVerdict.WARN);
            assertThat(AiVerdict.fromString("Need_Clarify")).isEqualTo(AiVerdict.NEED_CLARIFY);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"INVALID", "FAIL"})
        @DisplayName("fromString은 유효하지 않은 값에 대해 null을 반환해야 한다")
        void shouldReturnNullForInvalidString(String value) {
            assertThat(AiVerdict.fromString(value)).isNull();
        }

        @Test
        @DisplayName("validValuesString은 모든 유효한 값을 포함해야 한다")
        void shouldContainAllValidValues() {
            String validValues = AiVerdict.validValuesString();
            assertThat(validValues).contains("PASS");
            assertThat(validValues).contains("WARN");
            assertThat(validValues).contains("NEED_CLARIFY");
            assertThat(validValues).contains("NEED_FIX");
        }
    }

    @Nested
    @DisplayName("RiskLevel 검증")
    class RiskLevelValidationTest {

        @ParameterizedTest
        @ValueSource(strings = {"LOW", "MEDIUM", "HIGH"})
        @DisplayName("유효한 riskLevel 값은 true를 반환해야 한다")
        void shouldReturnTrueForValidRiskLevel(String value) {
            assertThat(RiskLevel.isValid(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"low", "medium", "high"})
        @DisplayName("소문자 riskLevel 값도 유효해야 한다")
        void shouldReturnTrueForLowercaseRiskLevel(String value) {
            assertThat(RiskLevel.isValid(value)).isTrue();
        }

        @ParameterizedTest
        @ValueSource(strings = {"CRITICAL", "NONE", "INVALID", "VERY_HIGH", ""})
        @DisplayName("유효하지 않은 riskLevel 값은 false를 반환해야 한다")
        void shouldReturnFalseForInvalidRiskLevel(String value) {
            assertThat(RiskLevel.isValid(value)).isFalse();
        }

        @Test
        @DisplayName("null riskLevel 값은 false를 반환해야 한다")
        void shouldReturnFalseForNullRiskLevel() {
            assertThat(RiskLevel.isValid(null)).isFalse();
        }

        @Test
        @DisplayName("fromString은 유효한 값을 enum으로 변환해야 한다")
        void shouldConvertValidStringToEnum() {
            assertThat(RiskLevel.fromString("LOW")).isEqualTo(RiskLevel.LOW);
            assertThat(RiskLevel.fromString("medium")).isEqualTo(RiskLevel.MEDIUM);
            assertThat(RiskLevel.fromString("High")).isEqualTo(RiskLevel.HIGH);
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {"INVALID", "CRITICAL"})
        @DisplayName("fromString은 유효하지 않은 값에 대해 null을 반환해야 한다")
        void shouldReturnNullForInvalidString(String value) {
            assertThat(RiskLevel.fromString(value)).isNull();
        }

        @Test
        @DisplayName("validValuesString은 모든 유효한 값을 포함해야 한다")
        void shouldContainAllValidValues() {
            String validValues = RiskLevel.validValuesString();
            assertThat(validValues).contains("LOW");
            assertThat(validValues).contains("MEDIUM");
            assertThat(validValues).contains("HIGH");
        }

        @Test
        @DisplayName("fromScore는 점수 범위에 따라 올바른 RiskLevel을 반환해야 한다")
        void shouldReturnCorrectRiskLevelFromScore() {
            // 기존 fromScore 메서드 검증
            assertThat(RiskLevel.fromScore(100)).isEqualTo(RiskLevel.LOW);
            assertThat(RiskLevel.fromScore(80)).isEqualTo(RiskLevel.LOW);
            assertThat(RiskLevel.fromScore(79)).isEqualTo(RiskLevel.MEDIUM);
            assertThat(RiskLevel.fromScore(50)).isEqualTo(RiskLevel.MEDIUM);
            assertThat(RiskLevel.fromScore(49)).isEqualTo(RiskLevel.HIGH);
            assertThat(RiskLevel.fromScore(0)).isEqualTo(RiskLevel.HIGH);
            assertThat(RiskLevel.fromScore(null)).isNull();
        }
    }
}
