package com.smartchain.platform.dto.review.export;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExportResponse {
    private String jobId;                // v3.0: 비동기 처리
    private String statusCheckUrl;
    private String message;
}
