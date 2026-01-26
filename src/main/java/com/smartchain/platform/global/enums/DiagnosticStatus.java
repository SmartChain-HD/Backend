package com.smartchain.platform.global.enums;

public enum DiagnosticStatus {
    WRITING,     // 작성중
    SUBMITTED,   // 제출됨 (결재자에게 제출)
    RETURNED,    // 반려됨 (보완 요청으로 반려)
    APPROVED,    // 내부승인 (협력사 내부 승인)
    REVIEWING,   // 심사중 (원청 심사 중)
    COMPLETED    // 완료 (최종 완료)
}
