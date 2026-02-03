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

    @Getter
    @Setter
    public static class SlotDefinition {
        private String name;
        private List<String> keywords = new ArrayList<>();
        private boolean required;
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
        String lowerName = fileName.toLowerCase();
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
}
