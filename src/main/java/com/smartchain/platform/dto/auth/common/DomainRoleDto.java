package com.smartchain.platform.dto.auth.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomainRoleDto {
    private String domainCode;
    private String domainName;
    private String roleCode;
    private String roleName;
}
