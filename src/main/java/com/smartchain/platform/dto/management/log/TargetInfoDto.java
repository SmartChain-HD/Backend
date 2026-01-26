package com.smartchain.platform.dto.management.log;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TargetInfoDto {
    private String type;                 // DIAGNOSTIC, FILE, USER
    private String id;
    private String description;
}
