package com.smartchain.platform.dto.review.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignedToDto {
    private Long userId;
    private String name;
    private String maskedName;
}
