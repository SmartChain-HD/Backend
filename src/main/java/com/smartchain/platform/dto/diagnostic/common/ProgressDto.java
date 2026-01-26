package com.smartchain.platform.dto.diagnostic.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProgressDto {
    private int qualitative;             // 정성적 진행률
    private int quantitative;            // 정량적 진행률
    private int overall;                 // 전체 진행률
}
