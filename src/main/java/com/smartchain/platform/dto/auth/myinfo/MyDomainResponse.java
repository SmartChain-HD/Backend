package com.smartchain.platform.dto.auth.myinfo;

import com.smartchain.platform.dto.auth.common.DomainRoleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyDomainResponse {
    private String globalRole;
    private List<DomainRoleDto> domainRoles;
    private String roleRequestStatus;
}
