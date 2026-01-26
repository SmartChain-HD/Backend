package com.smartchain.platform.dto.management.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticTemplateItemDto {
    private Long templateId;
    private String templateName;
    private String templateCode;
    private String version;
    private String description;
    private int questionCount;
    private int indicatorCount;
    private String status;               // ACTIVE, DRAFT, DEPRECATED
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
