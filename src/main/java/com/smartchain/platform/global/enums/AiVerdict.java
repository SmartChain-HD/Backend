package com.smartchain.platform.global.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * AI 분석 판정 결과
 * - PASS: 모든 필수 항목 검증 통과
 * - WARN: 경미한 이슈 발견
 * - NEED_CLARIFY: 추가 확인 필요
 * - NEED_FIX: 수정 필요 (필수 항목 미충족)
 */
public enum AiVerdict {
    PASS,
    WARN,
    NEED_CLARIFY,
    NEED_FIX;

    private static final Set<String> VALID_VALUES = Arrays.stream(values())
        .map(Enum::name)
        .collect(Collectors.toSet());

    /**
     * 문자열이 유효한 verdict 값인지 검증
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return VALID_VALUES.contains(value.toUpperCase());
    }

    /**
     * 문자열을 AiVerdict로 변환 (유효하지 않으면 null 반환)
     */
    public static AiVerdict fromString(String value) {
        if (value == null) {
            return null;
        }
        try {
            return valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * 유효한 값 목록을 문자열로 반환
     */
    public static String validValuesString() {
        return String.join(", ", VALID_VALUES);
    }
}
