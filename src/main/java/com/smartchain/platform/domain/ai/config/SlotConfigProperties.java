package com.smartchain.platform.domain.ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "ai.slots")
public class SlotConfigProperties {

    private Map<String, List<SlotDefinition>> domains = new HashMap<>();
    private List<CrossValidationDefinition> crossValidations = new ArrayList<>();

    @Getter
    @Setter
    public static class SlotDefinition {
        private String name;
        private String displayName;  // 한글 표시명
        private List<String> keywords = new ArrayList<>();
        private boolean required;
    }

    @Getter
    @Setter
    public static class CrossValidationDefinition {
        private String name;          // 가상 슬롯명 (예: esg.energy.electricity.month_match)
        private String displayName;   // 한글 표시명
        private List<String> slots = new ArrayList<>();  // 비교 대상 슬롯명
    }

    public List<SlotDefinition> getSlotsForDomain(String domainCode) {
        return domains.getOrDefault(domainCode.toLowerCase(), List.of());
    }

    public List<SlotDefinition> getRequiredSlots(String domainCode) {
        return getSlotsForDomain(domainCode).stream()
                .filter(SlotDefinition::isRequired)
                .toList();
    }

    public String matchSlotName(String fileName, String domainCode) {
        String lowerName = fileName.toLowerCase().replace('_', ' ');
        List<SlotDefinition> slots = getSlotsForDomain(domainCode);

        for (SlotDefinition slot : slots) {
            for (String keyword : slot.getKeywords()) {
                if (lowerName.contains(keyword.toLowerCase())) {
                    return slot.getName();
                }
            }
        }

        return domainCode.toLowerCase() + ".other";
    }

    /**
     * 슬롯명으로 표시명 조회
     */
    public String getDisplayName(String slotName, String domainCode) {
        return getSlotsForDomain(domainCode).stream()
            .filter(s -> s.getName().equals(slotName))
            .findFirst()
            .map(SlotDefinition::getDisplayName)
            .filter(dn -> dn != null && !dn.isBlank())
            .orElseGet(() -> {
                // *.other 슬롯은 "기타 문서"로 표시
                if (slotName != null && slotName.endsWith(".other")) {
                    return "기타 문서";
                }
                return slotName;
            });
    }

    /**
     * 교차 검증 슬롯인지 판별
     */
    public boolean isCrossValidation(String slotName) {
        return crossValidations.stream().anyMatch(cv -> cv.getName().equals(slotName));
    }

    /**
     * 교차 검증 정의 조회
     */
    public CrossValidationDefinition getCrossValidationDefinition(String slotName) {
        return crossValidations.stream()
            .filter(cv -> cv.getName().equals(slotName))
            .findFirst()
            .orElse(null);
    }
}
