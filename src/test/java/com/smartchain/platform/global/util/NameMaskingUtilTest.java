package com.smartchain.platform.global.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("NameMaskingUtil 테스트")
class NameMaskingUtilTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t"})
    @DisplayName("null, 빈 문자열, 공백은 그대로 반환")
    void mask_nullOrBlank_returnsAsIs(String input) {
        assertThat(NameMaskingUtil.mask(input)).isEqualTo(input);
    }

    @Test
    @DisplayName("1글자 이름은 그대로 반환")
    void mask_singleChar() {
        assertThat(NameMaskingUtil.mask("김")).isEqualTo("김");
    }

    @Test
    @DisplayName("2글자 이름은 첫 글자 + *")
    void mask_twoChars() {
        assertThat(NameMaskingUtil.mask("이수")).isEqualTo("이*");
    }

    @Test
    @DisplayName("3글자 이름은 첫 글자 + * + 마지막 글자")
    void mask_threeChars() {
        assertThat(NameMaskingUtil.mask("홍길동")).isEqualTo("홍*동");
    }

    @ParameterizedTest
    @CsvSource({
            "남궁선우, 남**우",
            "독고진세영, 독***영"
    })
    @DisplayName("4글자 이상 이름은 첫 글자 + * x (길이-2) + 마지막 글자")
    void mask_fourOrMoreChars(String input, String expected) {
        assertThat(NameMaskingUtil.mask(input)).isEqualTo(expected);
    }

    @Test
    @DisplayName("영문 이름도 동일한 규칙 적용")
    void mask_englishName() {
        assertThat(NameMaskingUtil.mask("John")).isEqualTo("J**n");
    }
}
