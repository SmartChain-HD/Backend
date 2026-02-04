package com.smartchain.platform.global.enums;

/**
 * 진단 상태 (DiagnosticStatus)
 *
 * <p>상태 전이 흐름:
 * <pre>
 * WRITING → SUBMITTED → RETURNED (반려 시)
 *                     ↓
 *                APPROVED → REVIEWING → COMPLETED
 * </pre>
 *
 * @see <a href="docs/STATUS_AND_ERROR_CODES.md">상태 코드 문서</a>
 */
public enum DiagnosticStatus {

    WRITING("작성중", "기안자가 진단 작성 중"),
    SUBMITTED("제출됨", "결재자에게 제출됨"),
    RETURNED("반려됨", "결재자가 반려함 (보완 요청)"),
    APPROVED("승인됨", "협력사 내부 승인 완료"),
    REVIEWING("심사중", "원청에서 심사 진행 중"),
    COMPLETED("완료", "심사 완료 (최종 상태)");

    private final String displayName;
    private final String description;

    DiagnosticStatus(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    /**
     * 프론트엔드 표시용 한글명
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * 상태 설명
     */
    public String getDescription() {
        return description;
    }
}
