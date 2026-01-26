package com.smartchain.platform.dto.diagnostic.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HighlightDto {
    private String type;                 // STRENGTH, WEAKNESS
    private String category;             // E, S, G
    private String description;
}
