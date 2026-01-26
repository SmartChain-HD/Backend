package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataSlotCategoryDto {
    private String categoryCode;         // E, S, G
    private String categoryName;         // "Environment"
    private List<DataSlotDto> requiredSlots;
    private List<DataSlotDto> optionalSlots;
}
