package com.smartchain.platform.dto.approval.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequesterDto {
    private Long userId;
    private String name;
}
