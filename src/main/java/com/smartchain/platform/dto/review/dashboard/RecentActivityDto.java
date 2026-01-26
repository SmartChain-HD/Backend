package com.smartchain.platform.dto.review.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentActivityDto {
    private String type;                 // SUBMITTED, APPROVED, REVISION_REQUIRED
    private String companyName;
    private String diagnosticCode;
    private LocalDateTime timestamp;
}
