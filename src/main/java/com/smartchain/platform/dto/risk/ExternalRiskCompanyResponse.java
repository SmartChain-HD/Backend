package com.smartchain.platform.dto.risk;

import com.smartchain.platform.domain.user.entity.Company;

public record ExternalRiskCompanyResponse(Long companyId, String name) {
    public static ExternalRiskCompanyResponse from(Company company) {
        return new ExternalRiskCompanyResponse(company.getCompanyId(), company.getName());
    }
}
