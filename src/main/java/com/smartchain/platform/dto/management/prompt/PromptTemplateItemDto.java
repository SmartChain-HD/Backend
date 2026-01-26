package com.smartchain.platform.dto.management.prompt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplateItemDto {
    private Long templateId;
    private String templateCode;         // RISK_ANALYSIS, REPORT_SUMMARY
    private String templateName;
    private String version;
    private String description;
    private String promptContent;        // 프롬프트 내용
    private boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
}
