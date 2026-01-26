package com.smartchain.platform.dto.management.company;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndustryInfoDto {
    private String code;                 // C29
    private String name;                 // 기타 기계 및 장비 제조업
}
