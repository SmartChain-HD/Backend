package com.smartchain.platform.dto.review.decision;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevisionRequestItemDto {
    private String category;             // E, S, G
    private String indicatorCode;
    private String indicatorName;        // v3.0: 추가
    private String description;          // v3.0: currentIssue → description
    private String requiredAction;       // 요청 조치
    private LocalDate deadline;
    private String priority;             // HIGH, MEDIUM, LOW
}
