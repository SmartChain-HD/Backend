package com.smartchain.platform.dto.diagnostic.quantitative;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuantCategoryDto {
    private String code;                 // E, S, G
    private String name;                 // "환경"
    private List<QuantIndicatorItemDto> indicators;
}
