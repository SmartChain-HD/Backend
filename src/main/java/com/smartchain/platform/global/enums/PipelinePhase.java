package com.smartchain.platform.global.enums;

/**
 * 파일 업로드 처리 파이프라인 단계
 * OCR -> VALIDATION -> METRICS 순서로 진행
 */
public enum PipelinePhase {
    QUEUED("대기 중", 0),
    OCR("OCR 처리 중", 1),
    VALIDATION("검증 중", 2),
    METRICS("메트릭 추출 중", 3),
    COMPLETED("완료", 4);

    private final String description;
    private final int order;

    PipelinePhase(String description, int order) {
        this.description = description;
        this.order = order;
    }

    public String getDescription() {
        return description;
    }

    public int getOrder() {
        return order;
    }

    /**
     * 다음 단계 반환. 마지막 단계면 COMPLETED 반환
     */
    public PipelinePhase next() {
        return switch (this) {
            case QUEUED -> OCR;
            case OCR -> VALIDATION;
            case VALIDATION -> METRICS;
            case METRICS, COMPLETED -> COMPLETED;
        };
    }

    /**
     * 현재 단계가 완료 단계인지 확인
     */
    public boolean isTerminal() {
        return this == COMPLETED;
    }
}
