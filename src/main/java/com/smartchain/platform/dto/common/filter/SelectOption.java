package com.smartchain.platform.dto.common.filter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SelectOption {
    private String value;
    private String label;
    private boolean disabled;
}
