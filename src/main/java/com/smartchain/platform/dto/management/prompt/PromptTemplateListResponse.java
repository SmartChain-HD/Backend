package com.smartchain.platform.dto.management.prompt;

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
public class PromptTemplateListResponse {
    private List<PromptTemplateItemDto> content;
    private PageDto page;
}
