package com.smartchain.platform.dto.management.kpi;

import com.smartchain.platform.dto.management.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KpiFormulaListResponse {
    private List<KpiFormulaItemDto> content;
    private PageDto page;
}
