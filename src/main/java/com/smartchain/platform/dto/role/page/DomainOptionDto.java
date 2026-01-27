package com.smartchain.platform.dto.role.page;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainOptionDto {
    private Long domainId;
    private String domainCode;
    private String domainName;
    private String description;
    private Boolean selectable;
}
