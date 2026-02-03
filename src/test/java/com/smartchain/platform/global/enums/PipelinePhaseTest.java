package com.smartchain.platform.global.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PipelinePhase Enum 테스트")
class PipelinePhaseTest {

    @Test
    @DisplayName("파이프라인 단계 순서가 올바르게 정의됨")
    void pipelinePhase_OrderIsCorrect() {
        assertThat(PipelinePhase.QUEUED.getOrder()).isEqualTo(0);
        assertThat(PipelinePhase.OCR.getOrder()).isEqualTo(1);
        assertThat(PipelinePhase.VALIDATION.getOrder()).isEqualTo(2);
        assertThat(PipelinePhase.METRICS.getOrder()).isEqualTo(3);
        assertThat(PipelinePhase.COMPLETED.getOrder()).isEqualTo(4);
    }

    @Test
    @DisplayName("다음 단계 전이가 올바르게 동작함")
    void pipelinePhase_NextTransitionIsCorrect() {
        assertThat(PipelinePhase.QUEUED.next()).isEqualTo(PipelinePhase.OCR);
        assertThat(PipelinePhase.OCR.next()).isEqualTo(PipelinePhase.VALIDATION);
        assertThat(PipelinePhase.VALIDATION.next()).isEqualTo(PipelinePhase.METRICS);
        assertThat(PipelinePhase.METRICS.next()).isEqualTo(PipelinePhase.COMPLETED);
        assertThat(PipelinePhase.COMPLETED.next()).isEqualTo(PipelinePhase.COMPLETED);
    }

    @Test
    @DisplayName("터미널 단계 확인이 올바르게 동작함")
    void pipelinePhase_IsTerminalIsCorrect() {
        assertThat(PipelinePhase.QUEUED.isTerminal()).isFalse();
        assertThat(PipelinePhase.OCR.isTerminal()).isFalse();
        assertThat(PipelinePhase.VALIDATION.isTerminal()).isFalse();
        assertThat(PipelinePhase.METRICS.isTerminal()).isFalse();
        assertThat(PipelinePhase.COMPLETED.isTerminal()).isTrue();
    }

    @Test
    @DisplayName("단계별 설명이 올바르게 정의됨")
    void pipelinePhase_DescriptionIsCorrect() {
        assertThat(PipelinePhase.QUEUED.getDescription()).isEqualTo("대기 중");
        assertThat(PipelinePhase.OCR.getDescription()).isEqualTo("OCR 처리 중");
        assertThat(PipelinePhase.VALIDATION.getDescription()).isEqualTo("검증 중");
        assertThat(PipelinePhase.METRICS.getDescription()).isEqualTo("메트릭 추출 중");
        assertThat(PipelinePhase.COMPLETED.getDescription()).isEqualTo("완료");
    }
}
