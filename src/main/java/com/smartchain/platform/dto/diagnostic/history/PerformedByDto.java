package com.smartchain.platform.dto.diagnostic.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PerformedByDto {
    private Long userId;
    private String name;
    private String role;
}
