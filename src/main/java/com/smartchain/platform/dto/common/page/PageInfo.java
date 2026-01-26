package com.smartchain.platform.dto.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Deprecated
public class PageInfo {
    private int currentPage;
    private int totalPages;
    private long totalElements;
    private int size;
    private boolean hasNext;
    private boolean hasPrevious;
    
    // v3.0: PageDto로 변환
    public PageDto toPageDto() {
        return PageDto.builder()
                .number(this.currentPage)
                .size(this.size)
                .totalElements(this.totalElements)
                .totalPages(this.totalPages)
                .build();
    }
}
