package com.smartchain.platform.dto.diagnostic.upload;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StandardSelectionRequest {
    @NotNull
    private Long diagnosticId;
    
    private List<String> standardCodes;  // ["GRI", "IFRS_S1"]
}
