package com.smartchain.platform.dto.diagnostic.upload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardOptionDto {
    private String standardCode;         // GRI, IFRS_S1, IFRS_S2
    private String standardName;         // "GRI", "IFRS S1/S2"
    private boolean selected;
}
