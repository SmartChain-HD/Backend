package com.smartchain.platform.dto.job;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class JobRetryResponse {
    private String newJobId;
    private String status;
    private String statusCheckUrl;
}
