package com.smartchain.platform.dto.diagnostic.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScoresDto {
    private int E;
    private int S;
    private int G;
}
