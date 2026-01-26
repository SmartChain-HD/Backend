package com.smartchain.platform.dto.common.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    private Integer page;        // 0부터 시작 (v3.0: 0-based 명시)
    private Integer size;        // 기본 10
    private String sort;         // v3.0: sortBy,sortDir → sort (예: "createdAt,desc")
}
