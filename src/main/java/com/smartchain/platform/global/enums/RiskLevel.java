package com.smartchain.platform.global.enums;

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
}
