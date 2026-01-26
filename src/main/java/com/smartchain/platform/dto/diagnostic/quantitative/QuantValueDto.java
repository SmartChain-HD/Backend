package com.smartchain.platform.dto.diagnostic.quantitative;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantValueDto {
    @NotNull
    private Long indicatorId;
    
    private BigDecimal value;
    private String source;               // MANUAL
}
