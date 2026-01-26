package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewStatusCardDto {
    private String reviewStatus;         // 심사 상태
    private String reviewStatusLabel;    // "승인"
    private String reviewStatusColorClass;
    private LocalDateTime lastUpdatedAt;
    private String lastUpdatedAtLabel;
}
