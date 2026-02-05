package com.smartchain.platform.dto.role.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessedByDto {
    private Long userId;
    private String name;
    private String maskedName;
}
