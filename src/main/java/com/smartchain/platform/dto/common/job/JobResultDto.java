package com.smartchain.platform.dto.common.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobResultDto {
    private Long fileId;
    private Integer extractedIndicators;
    private String resultUrl;
    private String downloadUrl;
}
