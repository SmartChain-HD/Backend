package com.smartchain.platform.global.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DiagnosticStatus Enum 테스트")
class DiagnosticStatusTest {

    @Test
    @DisplayName("모든 상태에 displayName이 정의됨")
    void diagnosticStatus_AllHaveDisplayName() {
        for (DiagnosticStatus status : DiagnosticStatus.values()) {
            assertThat(status.getDisplayName())
                    .as("Status %s should have displayName", status.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("상태별 displayName이 올바르게 정의됨")
    void diagnosticStatus_DisplayNameIsCorrect() {
        assertThat(DiagnosticStatus.WRITING.getDisplayName()).isEqualTo("작성중");
        assertThat(DiagnosticStatus.SUBMITTED.getDisplayName()).isEqualTo("제출됨");
        assertThat(DiagnosticStatus.RETURNED.getDisplayName()).isEqualTo("반려됨");
        assertThat(DiagnosticStatus.APPROVED.getDisplayName()).isEqualTo("승인됨");
        assertThat(DiagnosticStatus.REVIEWING.getDisplayName()).isEqualTo("심사중");
        assertThat(DiagnosticStatus.COMPLETED.getDisplayName()).isEqualTo("완료");
    }

    @Test
    @DisplayName("모든 상태에 description이 정의됨")
    void diagnosticStatus_AllHaveDescription() {
        for (DiagnosticStatus status : DiagnosticStatus.values()) {
            assertThat(status.getDescription())
                    .as("Status %s should have description", status.name())
                    .isNotNull()
                    .isNotBlank();
        }
    }

    @Test
    @DisplayName("상태 개수가 6개임 (워크플로우 일관성 검증)")
    void diagnosticStatus_CountIsSix() {
        assertThat(DiagnosticStatus.values()).hasSize(6);
    }

    @Test
    @DisplayName("상태 순서가 워크플로우와 일치함")
    void diagnosticStatus_OrderMatchesWorkflow() {
        DiagnosticStatus[] expected = {
                DiagnosticStatus.WRITING,
                DiagnosticStatus.SUBMITTED,
                DiagnosticStatus.RETURNED,
                DiagnosticStatus.APPROVED,
                DiagnosticStatus.REVIEWING,
                DiagnosticStatus.COMPLETED
        };
        assertThat(DiagnosticStatus.values()).containsExactly(expected);
    }
}
