package com.smartchain.platform.dto.management.system;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetentionPolicyUpdateRequest {
    private Integer diagnosticRetentionDays;
    private Integer fileRetentionDays;
    private Integer logRetentionDays;
    private Boolean autoDeleteEnabled;
}
