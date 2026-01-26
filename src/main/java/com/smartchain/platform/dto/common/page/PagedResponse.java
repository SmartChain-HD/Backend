package com.smartchain.platform.dto.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PagedResponse<T> {
    private List<T> content;     // v3.0: content로 통일
    private PageDto page;        // v3.0: pageInfo → page
}
