package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewLogItemDto {
    private Long logId;
    private LocalDate date;
    private String dateLabel;
    private String modifierName;
    private String field;
    private String action;
    private String status;
    private String previousValue;
    private String newValue;
    private boolean hasReason;
    private String reason;
}
