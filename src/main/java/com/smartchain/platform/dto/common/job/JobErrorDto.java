package com.smartchain.platform.dto.common.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobErrorDto {
    private String code;
    private String message;
    private boolean retryable;
}
