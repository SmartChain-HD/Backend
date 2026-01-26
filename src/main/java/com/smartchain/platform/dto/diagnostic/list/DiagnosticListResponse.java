package com.smartchain.platform.dto.diagnostic.list;

import com.smartchain.platform.dto.diagnostic.common.PageDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiagnosticListResponse {
    private List<DiagnosticListItemDto> content;  // v3.0: items → content
    private PageDto page;                // v3.0: 페이지네이션 표준화
}
