package com.smartchain.platform.dto.management.template;

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
public class DiagnosticTemplateListResponse {
    private List<DiagnosticTemplateItemDto> content;
    private PageDto page;
}
