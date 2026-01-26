package com.smartchain.platform.dto.management.company;

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
public class CompanyManagementResponse {
    private List<CompanyManagementItemDto> content;  // v3.0: companies → content
    private PageDto page;
}
