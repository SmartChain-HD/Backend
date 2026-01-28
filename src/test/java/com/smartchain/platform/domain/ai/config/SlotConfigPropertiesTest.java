package com.smartchain.platform.domain.ai.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SlotConfigPropertiesTest {

    private SlotConfigProperties properties;

    @BeforeEach
    void setUp() {
        properties = new SlotConfigProperties();

        // safety 도메인 슬롯 설정
        SlotConfigProperties.SlotDefinition safetyTbm = new SlotConfigProperties.SlotDefinition();
        safetyTbm.setName("safety.tbm");
        safetyTbm.setKeywords(List.of("tbm", "작업전", "안전점검"));
        safetyTbm.setRequired(true);

        SlotConfigProperties.SlotDefinition safetyEducation = new SlotConfigProperties.SlotDefinition();
        safetyEducation.setName("safety.education.status");
        safetyEducation.setKeywords(List.of("교육", "education", "이수"));
        safetyEducation.setRequired(true);

        // esg 도메인 슬롯 설정
        SlotConfigProperties.SlotDefinition esgEnergy = new SlotConfigProperties.SlotDefinition();
        esgEnergy.setName("esg.energy.usage");
        esgEnergy.setKeywords(List.of("에너지", "energy", "전력"));
        esgEnergy.setRequired(true);

        SlotConfigProperties.SlotDefinition esgEthics = new SlotConfigProperties.SlotDefinition();
        esgEthics.setName("esg.ethics.code");
        esgEthics.setKeywords(List.of("윤리", "ethics"));
        esgEthics.setRequired(false);

        properties.setDomains(Map.of(
                "safety", List.of(safetyTbm, safetyEducation),
                "esg", List.of(esgEnergy, esgEthics)
        ));
    }

    @Test
    @DisplayName("키워드가 포함된 파일명으로 슬롯을 매칭한다")
    void matchSlotName_matchesKeyword() {
        assertThat(properties.matchSlotName("TBM_점검표.pdf", "SAFETY"))
                .isEqualTo("safety.tbm");
        assertThat(properties.matchSlotName("안전교육_이수현황.xlsx", "SAFETY"))
                .isEqualTo("safety.education.status");
        assertThat(properties.matchSlotName("energy_usage_2024.csv", "ESG"))
                .isEqualTo("esg.energy.usage");
    }

    @Test
    @DisplayName("매칭되는 키워드가 없으면 domain.other를 반환한다")
    void matchSlotName_fallbackToOther() {
        assertThat(properties.matchSlotName("unknown_file.pdf", "SAFETY"))
                .isEqualTo("safety.other");
        assertThat(properties.matchSlotName("random.doc", "ESG"))
                .isEqualTo("esg.other");
    }

    @Test
    @DisplayName("존재하지 않는 도메인이면 domain.other를 반환한다")
    void matchSlotName_unknownDomain() {
        assertThat(properties.matchSlotName("test.pdf", "UNKNOWN"))
                .isEqualTo("unknown.other");
    }

    @Test
    @DisplayName("getSlotsForDomain은 해당 도메인의 슬롯 목록을 반환한다")
    void getSlotsForDomain_returnsList() {
        assertThat(properties.getSlotsForDomain("safety")).hasSize(2);
        assertThat(properties.getSlotsForDomain("nonexistent")).isEmpty();
    }

    @Test
    @DisplayName("getRequiredSlots는 required=true인 슬롯만 반환한다")
    void getRequiredSlots_filtersRequired() {
        List<SlotConfigProperties.SlotDefinition> required = properties.getRequiredSlots("esg");
        assertThat(required).hasSize(1);
        assertThat(required.get(0).getName()).isEqualTo("esg.energy.usage");
    }
}
