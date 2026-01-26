package com.smartchain.platform.dto.role.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDto {
    private int number;
    private int size;
    private long totalElements;
    private int totalPages;
}
