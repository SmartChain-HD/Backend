package com.smartchain.platform.domain.ai.service;

import com.smartchain.platform.domain.ai.config.SlotConfigProperties;
import com.smartchain.platform.dto.ai.run.SlotHint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiAnalysisServiceTest {

    @Mock
    private SlotConfigProperties slotConfigProperties;

    private AiAnalysisService aiAnalysisService;

    @BeforeEach
    void setUp() {
        // AiAnalysisService의 validateRequiredSlots 메서드만 테스트하기 위해
        // 다른 의존성은 null로 설정 (해당 메서드에서 사용하지 않음)
        aiAnalysisService = new AiAnalysisService(
            null, // AiRunApiClient
            null, // AiAnalysisResultRepository
            null, // DiagnosticRepository
            null, // EvidenceFileRepository
            null, // ObjectMapper
            slotConfigProperties
        );
    }

    @Nested
    @DisplayName("validateRequiredSlots")
    class ValidateRequiredSlotsTest {

        @Test
        @DisplayName("모든 필수 슬롯이 제출되면 빈 목록을 반환한다")
        void validateRequiredSlots_allSubmitted_returnsEmpty() {
            // given
            String domainCode = "ESG";
            List<SlotHint> slotHints = List.of(
                new SlotHint("1", "esg.energy.electricity.usage"),
                new SlotHint("2", "esg.energy.gas.usage"),
                new SlotHint("3", "esg.hazmat.msds"),
                new SlotHint("4", "esg.ethics.code")
            );

            List<SlotConfigProperties.SlotDefinition> requiredSlots = List.of(
                createSlotDefinition("esg.energy.electricity.usage"),
                createSlotDefinition("esg.energy.gas.usage"),
                createSlotDefinition("esg.hazmat.msds"),
                createSlotDefinition("esg.ethics.code")
            );

            when(slotConfigProperties.getRequiredSlots(domainCode)).thenReturn(requiredSlots);

            // when
            List<String> missingSlots = aiAnalysisService.validateRequiredSlots(slotHints, domainCode);

            // then
            assertThat(missingSlots).isEmpty();
        }

        @Test
        @DisplayName("일부 필수 슬롯이 누락되면 누락된 슬롯 목록을 반환한다")
        void validateRequiredSlots_someMissing_returnsMissingList() {
            // given
            String domainCode = "ESG";
            List<SlotHint> slotHints = List.of(
                new SlotHint("1", "esg.energy.electricity.usage"),
                new SlotHint("2", "esg.ethics.code")
            );

            List<SlotConfigProperties.SlotDefinition> requiredSlots = List.of(
                createSlotDefinition("esg.energy.electricity.usage"),
                createSlotDefinition("esg.energy.gas.usage"),
                createSlotDefinition("esg.hazmat.msds"),
                createSlotDefinition("esg.ethics.code")
            );

            when(slotConfigProperties.getRequiredSlots(domainCode)).thenReturn(requiredSlots);

            // when
            List<String> missingSlots = aiAnalysisService.validateRequiredSlots(slotHints, domainCode);

            // then
            assertThat(missingSlots)
                .hasSize(2)
                .containsExactlyInAnyOrder("esg.energy.gas.usage", "esg.hazmat.msds");
        }

        @Test
        @DisplayName("모든 필수 슬롯이 누락되면 전체 필수 슬롯 목록을 반환한다")
        void validateRequiredSlots_allMissing_returnsAllRequired() {
            // given
            String domainCode = "SAFETY";
            List<SlotHint> slotHints = List.of(
                new SlotHint("1", "safety.tbm"),  // 선택 슬롯만 제출
                new SlotHint("2", "safety.site.photos")  // 선택 슬롯
            );

            List<SlotConfigProperties.SlotDefinition> requiredSlots = List.of(
                createSlotDefinition("safety.education.status"),
                createSlotDefinition("safety.fire.inspection"),
                createSlotDefinition("safety.risk.assessment"),
                createSlotDefinition("safety.management.system")
            );

            when(slotConfigProperties.getRequiredSlots(domainCode)).thenReturn(requiredSlots);

            // when
            List<String> missingSlots = aiAnalysisService.validateRequiredSlots(slotHints, domainCode);

            // then
            assertThat(missingSlots)
                .hasSize(4)
                .containsExactlyInAnyOrder(
                    "safety.education.status",
                    "safety.fire.inspection",
                    "safety.risk.assessment",
                    "safety.management.system"
                );
        }

        @Test
        @DisplayName("필수 슬롯이 없는 도메인은 빈 목록을 반환한다")
        void validateRequiredSlots_noRequiredSlots_returnsEmpty() {
            // given
            String domainCode = "UNKNOWN";
            List<SlotHint> slotHints = List.of(
                new SlotHint("1", "unknown.file")
            );

            when(slotConfigProperties.getRequiredSlots(domainCode)).thenReturn(List.of());

            // when
            List<String> missingSlots = aiAnalysisService.validateRequiredSlots(slotHints, domainCode);

            // then
            assertThat(missingSlots).isEmpty();
        }

        @Test
        @DisplayName("중복된 슬롯 힌트가 있어도 정상적으로 검증한다")
        void validateRequiredSlots_duplicateHints_validatesCorrectly() {
            // given
            String domainCode = "COMPLIANCE";
            List<SlotHint> slotHints = List.of(
                new SlotHint("1", "compliance.contract.sample"),
                new SlotHint("2", "compliance.contract.sample"),  // 중복
                new SlotHint("3", "compliance.education.privacy"),
                new SlotHint("4", "compliance.fair.trade")
            );

            List<SlotConfigProperties.SlotDefinition> requiredSlots = List.of(
                createSlotDefinition("compliance.contract.sample"),
                createSlotDefinition("compliance.education.privacy"),
                createSlotDefinition("compliance.fair.trade")
            );

            when(slotConfigProperties.getRequiredSlots(domainCode)).thenReturn(requiredSlots);

            // when
            List<String> missingSlots = aiAnalysisService.validateRequiredSlots(slotHints, domainCode);

            // then
            assertThat(missingSlots).isEmpty();
        }

        private SlotConfigProperties.SlotDefinition createSlotDefinition(String name) {
            SlotConfigProperties.SlotDefinition slot = new SlotConfigProperties.SlotDefinition();
            slot.setName(name);
            slot.setRequired(true);
            return slot;
        }
    }
}
