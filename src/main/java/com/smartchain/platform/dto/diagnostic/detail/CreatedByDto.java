package com.smartchain.platform.dto.diagnostic.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatedByDto {
    private Long userId;
    private String name;
    private String maskedName;
    private String email;
}
