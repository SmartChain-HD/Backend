package com.smartchain.platform.dto.review.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDetailTabDto {
    private String tabCode;              // DIAGNOSTIC_PDF, DATA_PACKAGE, MODIFICATION_LOG, AI_REPORT
    private String tabName;
    private boolean isActive;
    private boolean hasContent;
}
