package com.smartchain.platform.dto.diagnostic.quantitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CurrentValueDto {
    private BigDecimal value;
    private String source;               // FILE_PARSING, MANUAL
    private Long sourceFileId;
    private Double confidence;
    private LocalDateTime updatedAt;
}
