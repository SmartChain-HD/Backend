package com.smartchain.platform.dto.management.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private int number;          // 현재 페이지 (0-based)
    private int size;            // 페이지 크기
    private long totalElements;  // 전체 항목 수
    private int totalPages;      // 전체 페이지 수
}
