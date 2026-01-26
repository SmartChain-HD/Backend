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
public class ReviewHistoryItemDto {
    private Long historyId;
    private String action;
    private String comment;
    private LocalDateTime timestamp;
    private String performedBy;
}
