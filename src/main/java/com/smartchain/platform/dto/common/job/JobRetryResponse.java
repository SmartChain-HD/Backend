package com.smartchain.platform.dto.common.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobRetryResponse {
    private String newJobId;
    private String status;               // PENDING
    private String statusCheckUrl;
}
