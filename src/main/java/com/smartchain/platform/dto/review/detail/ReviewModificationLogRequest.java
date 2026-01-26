package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewModificationLogRequest {
    private Long reviewId;               // v3.0: auditId → reviewId
    private LocalDate fromDate;
    private LocalDate toDate;
    private List<String> displayColumns;
}
