package com.smartchain.platform.global.enums;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 위험군 등급
 * - LOW: 저위험 (80-100점)
 * - MEDIUM: 중위험 (50-79점)
 * - HIGH: 고위험 (0-49점)
 */
public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH;

    private static final Set<String> VALID_VALUES = Arrays.stream(values())
        .map(Enum::name)
        .collect(Collectors.toSet());

    public static RiskLevel fromScore(Integer score) {
        if (score == null) {
            return null;
        }
        if (score >= 80) {
            return LOW;
        } else if (score >= 50) {
            return MEDIUM;
        } else {
            return HIGH;
        }
    }

    /**
     * 문자열이 유효한 riskLevel 값인지 검증
     */
    public static boolean isValid(String value) {
        if (value == null) {
            return false;
        }
        return VALID_VALUES.contains(value.toUpperCase());
    }

    /**
     * 문자열을 RiskLevel로 변환 (유효하지 않으면 null 반환)
     */
    public static RiskLevel fromString(String value) {
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
