package com.smartchain.platform.global.enums;

/**
 * 알림 타입
 */
public enum NotificationType {
    APPROVAL_COMPLETE,      // 결재 완료
    REVIEW_COMPLETE,        // 심사 완료
    ROLE_APPROVED,          // 권한 승인
    REVISION_REQUIRED,      // 보완 요청
    AI_ANALYSIS_COMPLETE,   // AI 분석 완료
    AI_ANALYSIS_FAILED      // AI 분석 실패
}
