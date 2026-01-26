package com.smartchain.platform.dto.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FieldErrorDto {
    private String field;
    private Object rejectedValue;
    private String reason;
}
